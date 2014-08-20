package me.lightspeed7.mongofs.crypto;

import java.security.GeneralSecurityException;

import me.lightspeed7.mongofs.util.ChunkSize;

public interface Crypto {

    /**
     * The size of the buffer of data to encrypt
     * 
     * NOTE : This value must be <= the chunkSize for the collection to keep the system performant.
     * 
     * @return the ChunkSize
     */
    ChunkSize getChunkSize();

    /**
     * Return the given bytes in their encrypted form
     * 
     * @param dataIn
     * @param offset
     * @param length
     * @return the encrypted bytes
     * @throws GeneralSecurityException
     */
    byte[] encrypt(byte[] dataIn, int offset, int length) throws GeneralSecurityException;

    /**
     * Return the given encrypted bytes back in their original form
     * 
     * @param dataIn
     * @param offset
     * @param length
     * @return the un-encrypted bytes
     * @throws GeneralSecurityException
     */
    byte[] decrypt(byte[] dataIn, int offset, int length) throws GeneralSecurityException;

}
