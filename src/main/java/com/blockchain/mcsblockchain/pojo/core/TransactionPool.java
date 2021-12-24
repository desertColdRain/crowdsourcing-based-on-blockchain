package com.blockchain.mcsblockchain.pojo.core;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
//交易池
public class TransactionPool {

    private List<Transaction> transactions=new ArrayList<>();
    //添加交易
    public void addTransaction(Transaction transaction){
        boolean exist=false;
        for(Transaction tx:transactions){
            if(Objects.equals(tx.getTransactionHash(), transaction.getTransactionHash())) {
                exist=true;
                break;
            }

        }
        if(!exist){
            transactions.add(transaction);
        }
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }
     public void removeTransaction(String txHash){
         for (Iterator<Transaction> i = transactions.iterator(); i.hasNext();) {
             Transaction tx = (Transaction) i.next();
             if (Objects.equals(tx.getTransactionHash(), txHash)) {
                 i.remove();
             }
         }
     }
}