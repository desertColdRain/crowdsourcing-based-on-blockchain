package com.blockchain.mcsblockchain.net.server;

import com.blockchain.mcsblockchain.Utils.Cryptography;
import com.blockchain.mcsblockchain.Utils.SerializeUtils;
import com.blockchain.mcsblockchain.conf.AppConfig;
import com.blockchain.mcsblockchain.net.base.*;
import com.blockchain.mcsblockchain.pojo.core.Block;
import com.blockchain.mcsblockchain.pojo.core.Transaction;
import com.blockchain.mcsblockchain.pojo.core.TransactionExecutor;
import com.blockchain.mcsblockchain.pojo.core.TransactionPool;
import com.blockchain.mcsblockchain.pojo.db.DBAccess;
import com.google.common.base.Objects;
import com.google.common.base.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.tio.core.Aio;
import org.tio.core.ChannelContext;
import org.tio.core.intf.Packet;
import org.tio.server.intf.ServerAioHandler;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

//服务端 AioHandler 实现
@Component
public class AppServerAioHandler extends BaseAioHandler implements ServerAioHandler {

    private static final Logger logger = LoggerFactory.getLogger(AppServerAioHandler.class);
    @Autowired
    private DBAccess dbAccess;
    @Autowired
    private TransactionPool transactionPool;
    @Autowired
    private TransactionExecutor executor;
    @Autowired
    private AppConfig appConfig;

    public AppServerAioHandler() {
    }

    // 处理消息
    @Override
    public void handler(Packet packet, ChannelContext channelContext) throws Exception
    {
        MessagePacket messagePacket = (MessagePacket) packet;
        byte type = messagePacket.getType();
        byte[] body = messagePacket.getBody();

        if (body != null) {
            logger.info("请求节点信息， {}", channelContext.getClientNode());
            if (body != null) {
                MessagePacket resPacket = null;
                switch (type) {

                    //普通字符串消息
                    case MessagePacketType.STRING_MESSAGE:
                        resPacket = this.stringMessage(body);
                        break;

                    // 确认交易
                    case MessagePacketType.REQ_CONFIRM_TRANSACTION:
                        resPacket = this.confirmTransaction(body);
                        break;

                    // 同步下一个区块
                    case MessagePacketType.REQ_SYNC_NEXT_BLOCK:
                        resPacket = this.fetchNextBlock(body);
                        break;

                    // 新区快确认
                    case MessagePacketType.REQ_NEW_BLOCK:
                        resPacket = this.newBlock(body);
                        break;

                    //获取节点列表
                    case MessagePacketType.REQ_NODE_LIST:
                        resPacket = this.getNodeList(body);
                        break;

                    case MessagePacketType.RES_INC_CONFIRM_NUM:
                        resPacket = this.incBlockConfirmNum(body);
                        break;

                    case MessagePacketType.REQ_NODE_TRANSACTION_POOL:
                        resPacket=this.getTransactionPool(body);

                } //end of switch
                //发送消息
                Aio.send(channelContext, resPacket);
            }
        }
    }

    /**
     普通字符串消息
     */
    public MessagePacket stringMessage(byte[] body) throws IOException, ClassNotFoundException {

        MessagePacket resPacket = new MessagePacket();
        String str = (String) SerializeUtils.deserialize(body);
        logger.info("收到客户端请求消息："+str);
        resPacket.setType(MessagePacketType.STRING_MESSAGE);
        resPacket.setBody(SerializeUtils.serialize("收到了你的消息，你的消息是:" + str));

        return resPacket;
    }

    /**
     * 确认交易
     * @param body
     */
    public MessagePacket confirmTransaction(byte[] body) throws Exception {

        ServerResponseVo responseVo = new ServerResponseVo();
        MessagePacket resPacket = new MessagePacket();
        Transaction tx = (Transaction) SerializeUtils.deserialize(body);
        logger.info("收到交易确认请求， {}", tx);
        responseVo.setItem(tx);
        //验证交易
        String msg=    "senderPk=" + tx.getSenderPk() +
                ", receiverPk=" + tx.getReceiverPk() +
                ", transactionType=" + tx.getTransactionType() +
                ", content='" + tx.getContent() + '\'' +
                ", receiverAddr='" + tx.getReceiverAddr() + '\'' +
                ", amount=" + tx.getAmount() +
                ", blockNum=" + tx.getBlockNum()+
                '}';
        if (Cryptography.SignatureVerify(tx.toString(), tx.getSenderPk(), tx.getSenderSign())) {
            responseVo.setSuccess(true);
            //将交易放入交易池
            transactionPool.addTransaction(tx);
        }
        else {
            responseVo.setSuccess(false);
            responseVo.setMessage("交易签名错误");
            logger.info("交易确认失败, 交易签名错误, {}", tx);
        }
        resPacket.setType(MessagePacketType.RES_CONFIRM_TRANSACTION);
        resPacket.setBody(SerializeUtils.serialize(responseVo));

        return resPacket;
    }

    /**
     * 获取下一个区块
     *
     */
    public MessagePacket fetchNextBlock(byte[] body) throws IOException, ClassNotFoundException {

        ServerResponseVo responseVo = new ServerResponseVo();
        MessagePacket resPacket = new MessagePacket();
        Integer blockIndex = (Integer) SerializeUtils.deserialize(body);
        logger.info("收到区块同步请求, 同步区块高度为， {}", blockIndex);
        Optional<Block> block = dbAccess.getBlock(blockIndex);
        if (block.isPresent()) {
            responseVo.setItem(block.get());
            responseVo.setSuccess(true);
        } else {
            responseVo.setSuccess(false);
            responseVo.setItem(null);
            responseVo.setMessage("要同步的区块不存在.{"+blockIndex+"}");
        }
        resPacket.setType(MessagePacketType.RES_SYNC_NEXT_BLOCK);
        resPacket.setBody(SerializeUtils.serialize(responseVo));

        return resPacket;
    }

    public MessagePacket newBlock(byte[] body) throws Exception {

        ServerResponseVo responseVo = new ServerResponseVo();
        MessagePacket resPacket = new MessagePacket();
        Block newBlock = (Block) SerializeUtils.deserialize(body);
        logger.info("收到新区块确认请求： {}", newBlock);
        if (checkBlock(newBlock, dbAccess)) {
            dbAccess.putLastBlockIndex(newBlock.getHeader().getIndex());
            dbAccess.putBlock(newBlock);
            responseVo.setSuccess(true);
            //执行区块中的交易 ，同步账户的余额
            executor.run(newBlock);
        } else {
            logger.error("区块确认失败：{}", newBlock);
            responseVo.setSuccess(false);
            responseVo.setMessage("区块校验失败，不合法的区块.");
        }
        responseVo.setItem(newBlock);
        resPacket.setType(MessagePacketType.RES_NEW_BLOCK);
        resPacket.setBody(SerializeUtils.serialize(responseVo));

        return resPacket;
    }

    /**
     * 获取节点列表

     */
    public MessagePacket getNodeList(byte[] body) throws IOException, ClassNotFoundException {
        String message = (String) SerializeUtils.deserialize(body);
        ServerResponseVo responseVo = new ServerResponseVo();
        MessagePacket resPacket = new MessagePacket();
        logger.info("收到获取节点列表请求");
        if (Objects.equal(message, MessagePacket.FETCH_NODE_LIST_SYMBOL)) {
            Optional<List<Node>> nodes = dbAccess.getNodeList();
            if (nodes.isPresent()) {
                responseVo.setSuccess(true);
                responseVo.setItem(nodes.get());
            }
        } else {
            responseVo.setSuccess(false);
        }
        resPacket.setType(MessagePacketType.RES_NODE_LIST);
        resPacket.setBody(SerializeUtils.serialize(responseVo));

        return  resPacket;
    }

    public MessagePacket getTransactionPool(byte[] body) throws IOException, ClassNotFoundException {
        ServerResponseVo responseVo = new ServerResponseVo();
        TransactionPool txPool = (TransactionPool) SerializeUtils.deserialize(body);
        MessagePacket resPacket = new MessagePacket();
        logger.info("收到获取节点交易池请求");
        TransactionPool allTxs = dbAccess.getTxPool();
        if(allTxs!=null) {
            responseVo.setItem(allTxs);
            responseVo.setSuccess(true);
        }
        else{
            responseVo.setSuccess(false);
            responseVo.setMessage("请求的交易池不存在");
        }

        resPacket.setType(MessagePacketType.RES_NODE_TRANSACTION_POOL);
        resPacket.setBody(SerializeUtils.serialize(responseVo));
        return resPacket;
    }
    public MessagePacket incBlockConfirmNum(byte[] body) throws IOException, ClassNotFoundException {
        ServerResponseVo responseVo = new ServerResponseVo();
        MessagePacket resPacket = new MessagePacket();
        Integer blockIndex = (Integer) SerializeUtils.deserialize(body);
        logger.info("收到增加区块确认数请求, 同步区块高度为， {}", blockIndex);
        Optional<Block> blockOptional = dbAccess.getBlock(blockIndex);
        if (blockOptional.isPresent()) {
            Block block = blockOptional.get();
            // 增加区块确认数
            block.setConfirmNum(block.getConfirmNum()+1);

            if (block.getConfirmNum() >= appConfig.getMinConfirmNum()) {
                // 更改当前区块所有的交易状态
                for (Transaction transaction : block.getBody().getTransactionList()) {
                    transaction.setStatus(0);
                }
            }
            dbAccess.putBlock(block); // 更新区块
            responseVo.setSuccess(true);
        } else {
            responseVo.setSuccess(false);
            responseVo.setMessage("区块高度不存在.{"+blockIndex+"}");
        }
        resPacket.setType(MessagePacketType.RES_INC_CONFIRM_NUM);
        resPacket.setBody(SerializeUtils.serialize(responseVo));

        return resPacket;
    }
}