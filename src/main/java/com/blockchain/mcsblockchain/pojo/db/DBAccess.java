package com.blockchain.mcsblockchain.pojo.db;

import com.blockchain.mcsblockchain.pojo.account.Account;
import com.blockchain.mcsblockchain.pojo.core.Block;
import com.blockchain.mcsblockchain.pojo.net.base.Node;
import com.blockchain.mcsblockchain.pojo.core.Transaction;

import java.io.IOException;
import java.util.List;
import com.google.common.base.Optional;

public interface DBAccess {
    //区块数据存储 hash 桶前缀
    String BLOCKS_BUCKET_PREFIX = "blocks_";
   //钱包数据存储 hash 桶前缀
    String WALLETS_BUCKET_PREFIX = "wallets_";
    //挖矿账户
    String MINER_ACCOUNT = "miner_account";
    //最后一个区块的区块高度
    String LAST_BLOCK_INDEX = BLOCKS_BUCKET_PREFIX+"last_block";
    //客户端节点列表存储 key
    String CLIENT_NODES_LIST_KEY = "client-node-list";
    //更新最新一个区块的hash值
    boolean putLastBlockIndex(Object lastBlock);
    //获取最新区块的hash值
    Optional<Object> getLastBlockIndex();
    //保存区块
    boolean putBlock(Block block);
    //获取指定的区块
    Optional<Block> getBlock(Object index);
    //获取最后（高度最大）的一个区块
    Optional<Block> getLastBlock();
    //添加一个账户钱包
    boolean putAccount(Account account);
    //获取指定的账户
    Optional<Account> getAccount(String address);
    //获取所有账户列表
    List<Account> getAllAccounts() throws IOException, ClassNotFoundException;
    //获取挖矿账户
    Optional<Account> getMinerAccount();
    //设置挖矿账户
    boolean setMinerAccount(Account account);
    //获取客户端节点列表
    Optional<List<Node>> getNodeList();
    //添加一个客户端节点
    //boolean addNode(Node node);
    //往数据库添加一条数据
    boolean put(String key, Object value);
    //获取某一条指定的数据
    Optional<Object> get(String key);
    //删除一条数据
    boolean delete(String key);
    //根据前缀搜索
    <T> List<T> seekByKey(String keyPrefix) throws IOException, ClassNotFoundException;
    //根据交易哈希搜索交易
    Transaction getTransactionByTxHash(String txHash);
    void closeDB();
}