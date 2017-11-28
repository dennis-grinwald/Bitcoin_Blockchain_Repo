package assignment1starterCode;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import assignment1starterCode.Transaction.Input;
import assignment1starterCode.Transaction.Output;

public class TxHandler {


	private UTXOPool utxoPool;
	private Transaction[] txList;

	/**
     * Creates a public ledger whose current UTXOPool (collection of unspent transaction outputs) is
     * {@code utxoPool}. This should make a copy of utxoPool by using the UTXOPool(UTXOPool uPool)
     * constructor.
     */
    public TxHandler(UTXOPool utxoPool) {
    	this.utxoPool = new UTXOPool(utxoPool);
    }

    /**
     * @return true if:
     * (1) all outputs claimed by {@code tx} are in the current UTXO pool, - Done
     * (2) the signatures on each input of {@code tx} are valid, - Done
     * (3) no UTXO is claimed multiple times by {@code tx}, - Done
     * (4) all of {@code tx}s output values are non-negative, and - Done
     * (5) the sum of {@code tx}s input values is greater than or equal to the sum of its output
     *     values; and false otherwise. - Done
     */
 // remember that the utxoPool contains a single unspent Transaction.Output which
 		// is the coin from Scrooge 
    public boolean isValidTx(Transaction tx) {
    	ArrayList<UTXO> utxos = utxoPool.getAllUTXO();
    	ArrayList<Output> outputs = tx.getOutputs();
    	ArrayList<Input> inputs = tx.getInputs();
    	ArrayList<UTXO> singleUTXOS = new ArrayList<UTXO>();
    	
    	double inputSum = 0;
    	double outputSum = 0;
    	 
    	
    	int i = 0 ;
    	for(Transaction.Input input : inputs) {
    		UTXO utxo = new UTXO(input.prevTxHash,input.outputIndex);
    		Transaction.Output otp = utxoPool.getTxOutput(utxo);
    		if(!utxos.contains(utxo)) return false;
    		if(!Crypto.verifySignature(otp.address, tx.getRawDataToSign(i), input.signature)) return false;
    		if(singleUTXOS.contains(utxo)) return false;
    		singleUTXOS.add(utxo);
    		inputSum+=otp.value;
    	}
    	
    	int j=0;
    	for(Transaction.Output otp : outputs) {
    		outputSum += tx.getOutput(j).value;
    		j++;	
    	}
    	return inputSum>=outputSum;
    	
    }
    	

    /**
     * Handles each epoch by receiving an unordered array of proposed transactions, checking each
     * transaction for correctness, returning a mutually valid array of accepted transactions, and
     * updating the current UTXO pool as appropriate.
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs) {
    	
    	this.txList = possibleTxs;
    	ArrayList<Transaction> emptyTxs = new ArrayList<Transaction>();
    	
    	for(Transaction txs : possibleTxs) {
    		TxHandler tester = new TxHandler(utxoPool);
    		if(tester.isValidTx(txs)) {
    			emptyTxs.add(txs);
    			for(Transaction.Input in : txs.getInputs()) {
    				utxoPool.removeUTXO(new UTXO(in.prevTxHash, in.outputIndex));
    			}
    			for(int i = 0; i < txs.numOutputs(); i++) {
    				Transaction.Output otp1 = txs.getOutput(i);
    				UTXO utxo = new UTXO(txs.getHash(), i);
    				utxoPool.addUTXO(utxo, otp1);
    			}
    		}
    	}
    	
    	
    Transaction[] validTxArray = new Transaction[emptyTxs.size()];
    return emptyTxs.toArray(validTxArray);

    }
    
}
   
