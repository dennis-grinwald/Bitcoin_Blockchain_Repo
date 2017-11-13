package assignment1starterCode;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import assignment1starterCode.Transaction.Input;
import assignment1starterCode.Transaction.Output;

public class TxHandler {


	private UTXOPool utxoPool;

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
     * (1) all outputs claimed by {@code tx} are in the current UTXO pool, - DONE
     * (2) the signatures on each input of {@code tx} are valid, - DONE
     * (3) no UTXO is claimed multiple times by {@code tx}, - DONE
     * (4) all of {@code tx}s output values are non-negative, and - DONE
     * (5) the sum of {@code tx}s input values is greater than or equal to the sum of its output
     *     values; and false otherwise. - DONE
     */
    public boolean isValidTx(Transaction tx) {
    	double inputSum = 0;
    	double outputSum = 0;
    	ArrayList<Output> outputs = tx.getOutputs();
    	ArrayList<Input> inputs = tx.getInputs();
    	ArrayList<UTXO> utxos = utxoPool.getAllUTXO();
    	UTXOPool singleUTXOs = new UTXOPool(); 
    	
    	//solution: (1),(4)
    	for (Output otp : outputs) {
    		if (!utxos.contains(otp)) { return false ;}
    		if (otp.value < 0) {return false;}
    		outputSum += otp.value;
    	}
    	
    	//solution: (2),(3)
    	int i = 0;
    	for (Input inpt : inputs) {
    		UTXO previousUTXO = new UTXO(inpt.prevTxHash, inpt.outputIndex);
    		Transaction.Output previousOutput = utxoPool.getTxOutput(previousUTXO);
    		if (!Crypto.verifySignature(previousOutput.address, tx.getRawDataToSign(i), inpt.signature)) {return false;}
    		if (!utxoPool.contains(previousUTXO)) {return false;}
    		if (singleUTXOs.contains(previousUTXO)) {return false;}
    		singleUTXOs.addUTXO(previousUTXO, previousOutput);
    		inputSum += previousOutput.value;
    		i++;
    	}
    	
    	
    	
    //solution (5)
    	return inputSum >= outputSum;
    }

    /**
     * Handles each epoch by receiving an unordered array of proposed transactions, checking each
     * transaction for correctness, returning a mutually valid array of accepted transactions, and
     * updating the current UTXO pool as appropriate.
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs) {
    	Set<Transaction> validTransactions = new HashSet<>();
    	for (Transaction tx : possibleTxs) {
    		if (isValidTx(tx)) {
    			validTransactions.add(tx);
    			for (Transaction.Input in : tx.getInputs()) {
    				UTXO utxo = new UTXO(in.prevTxHash, in.outputIndex);
                 utxoPool.removeUTXO(utxo);
    			}
    			for (int i = 0; i < tx.numOutputs(); i++) {
                    Transaction.Output out = tx.getOutput(i);
                    UTXO utxo = new UTXO(tx.getHash(), i);
                    utxoPool.addUTXO(utxo, out);
    		
    			}
    			
    			
    		}
    	}
    	
    		
    			Transaction[] validTxArray = new Transaction[validTransactions.size()];
    	        return validTransactions.toArray(validTxArray);
    	}
}
