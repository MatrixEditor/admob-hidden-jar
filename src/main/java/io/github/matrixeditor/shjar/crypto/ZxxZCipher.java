package io.github.matrixeditor.shjar.crypto;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.*;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Base64;

public class ZxxZCipher extends CipherSpi {

    /**
     * A simple Object that synchronizes all incoming callers
     * when the AES-{@link Cipher} should be initialized.
     */
    private final Object aesInitLock = new Object();

    /**
     * A simple Object that synchronizes all incoming callers
     * when the AES-{@link Cipher} is created
     */
    private final Object aesWriteLock = new Object();

    /**
     * AES/CBC/PKCS5Padding Cipher which is used to de- and
     * encrypt incoming data.
     */
    private Cipher aes;

    /**
     * The current cipher state.
     */
    private volatile int mode;

    /**
     * The {@link SecretKeySpec} for the AES algorithm.
     */
    private SecretKey key;

    /**
     * Decodes the given input {@link String} with a {@link Base64.Decoder} and
     * applies a simple XOR mechanism on the returned byte array.
     *
     * @param content the encoded key
     * @return the decoded content with a length of 16 bytes
     * @throws IllegalArgumentException if the decoded input does not match a length
     *                                  of 32 bytes.
     */
    public static byte[] decodeXOR(String content) throws IllegalArgumentException {
        return decodeXOR(content.getBytes());
    }

    /**
     * Decodes the given input bytes with a {@link Base64.Decoder} and
     * applies a simple XOR mechanism on the returned byte array.
     *
     * @param content the encoded key
     * @return the decoded content with a length of 16 bytes
     * @throws IllegalArgumentException if the decoded input does not match a length
     *                                  of 32 bytes.
     */
    public static byte[] decodeXOR(byte[] content) throws IllegalArgumentException {
        byte[] decoded = Base64.getDecoder().decode(content);
        if (decoded.length != 32) {
            throw new IllegalArgumentException("decoded.length != 32");
        }

        byte[] result = new byte[16];
        ByteBuffer.wrap(decoded, 4, 16).get(result);
        for (int i = 0; i < 16; i++) {
            result[i] = (byte) (result[i] ^ 68);
        }
        return result;
    }

    /**
     * Decrypts the given bytes by applying the stored {@link SecretKey}.
     *
     * @param bytes the encrypted content
     * @return a byte array representing the decrypted content.
     * @throws IllegalBlockSizeException if the decoded content is smaller than 16 bytes
     */
    private synchronized byte[] doDecrypt(byte[] bytes) throws IllegalBlockSizeException {
        byte[] result;
        byte[] iv;
        byte[] content;
        byte[] secretKey = key.getEncoded();

        if (secretKey.length != 16) {
            throw new IllegalBlockSizeException("Key.length != 16");
        }

        try {
            byte[] decoded = Base64.getDecoder().decode(bytes);
            if (decoded.length <= 16) {
                throw new BadPaddingException("Content.length <= 16");
            }

            ByteBuffer buffer = ByteBuffer.allocate(decoded.length);
            buffer.put(decoded).flip();

            iv = new byte[16];
            content = new byte[decoded.length - 16];
            buffer.get(iv);
            buffer.get(content);

            synchronized (aesInitLock) {
                aes.init(mode, key, new IvParameterSpec(iv));
                result = getAESCipher().doFinal(content);
            }
            return result;
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Encrypts the given bytes by applying the stored {@link SecretKey}.
     *
     * @param bytes the plain content
     * @return a byte array representing the encrypted content.
     * @throws IllegalBlockSizeException if the key length is not 16 bytes
     */
    private synchronized byte[] doEncrypt(byte[] bytes) throws IllegalBlockSizeException {
        byte[] result;
        byte[] iv;
        byte[] secretKey = key.getEncoded();

        if (secretKey.length != 16) {
            throw new IllegalBlockSizeException("Key.length != 16");
        }

        try {
            synchronized (aesInitLock) {
                getAESCipher().init(mode, key, (SecureRandom) null);
                result = getAESCipher().doFinal(bytes);
                iv = engineGetIV();
            }

            int len = result.length + iv.length;
            ByteBuffer buffer = ByteBuffer.allocate(len);
            buffer.put(iv);
            buffer.put(result).flip();

            byte[] finalResult = new byte[len];
            buffer.get(finalResult);
            return Base64.getEncoder().encode(finalResult);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(e);
        }
    }

    // CipherSpi implementation

    /**
     * Sets the mode of this cipher.
     *
     * @param mode the cipher mode
     * @throws NoSuchAlgorithmException if the requested cipher mode does
     *                                  not exist
     */
    @Override
    protected void engineSetMode(String mode) throws NoSuchAlgorithmException {
        throw new UnsupportedOperationException();
    }

    /**
     * Sets the padding mechanism of this cipher.
     *
     * @param padding the padding mechanism
     * @throws NoSuchPaddingException if the requested padding mechanism
     *                                does not exist
     */
    @Override
    protected void engineSetPadding(String padding) throws NoSuchPaddingException {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns the block size (in bytes).
     *
     * @return the block size (in bytes), or 0 if the underlying algorithm is
     * not a block cipher
     */
    @Override
    protected int engineGetBlockSize() {
        return getAESCipher().getBlockSize();
    }

    /**
     * Returns the length in bytes that an output buffer would
     * need to be in order to hold the result of the next <code>update</code>
     * or <code>doFinal</code> operation, given the input length
     * <code>inputLen</code> (in bytes).
     *
     * <p>This call takes into account any unprocessed (buffered) data from a
     * previous <code>update</code> call, padding, and AEAD tagging.
     *
     * <p>The actual output length of the next <code>update</code> or
     * <code>doFinal</code> call may be smaller than the length returned by
     * this method.
     *
     * @param inputLen the input length (in bytes)
     * @return the required output buffer size (in bytes)
     */
    @Override
    protected int engineGetOutputSize(int inputLen) {
        return getAESCipher().getOutputSize(inputLen);
    }

    /**
     * Returns the initialization vector (IV) in a new buffer.
     *
     * <p> This is useful in the context of password-based encryption or
     * decryption, where the IV is derived from a user-provided passphrase.
     *
     * @return the initialization vector in a new buffer, or null if the
     * underlying algorithm does not use an IV, or if the IV has not yet
     * been set.
     */
    @Override
    protected byte[] engineGetIV() {
        return getAESCipher().getIV();
    }

    /**
     * Returns the parameters used with this cipher.
     *
     * <p>The returned parameters may be the same that were used to initialize
     * this cipher, or may contain a combination of default and random
     * parameter values used by the underlying cipher implementation if this
     * cipher requires algorithm parameters but was not initialized with any.
     *
     * @return the parameters used with this cipher, or null if this cipher
     * does not use any parameters.
     */
    @Override
    protected AlgorithmParameters engineGetParameters() {
        throw new UnsupportedOperationException();
    }

    /**
     * Initializes this cipher with a key and a source
     * of randomness.
     *
     * <p>Note that when a Cipher object is initialized, it loses all
     * previously-acquired state. In other words, initializing a Cipher is
     * equivalent to creating a new instance of that Cipher and initializing
     * it.
     *
     * @param opmode the operation mode of this cipher (this is one of
     *               the following:
     *               <code>ENCRYPT_MODE</code>, <code>DECRYPT_MODE</code>,
     *               <code>WRAP_MODE</code> or <code>UNWRAP_MODE</code>)
     * @param key    the encryption key
     * @param random the source of randomness
     * @throws InvalidKeyException           if the given key is inappropriate for
     *                                       initializing this cipher, or requires
     *                                       algorithm parameters that cannot be
     *                                       determined from the given key.
     * @throws UnsupportedOperationException if {@code opmode} is
     *                                       {@code WRAP_MODE} or {@code UNWRAP_MODE} is not implemented
     *                                       by the cipher.
     */
    @Override
    protected void engineInit(int opmode, Key key, SecureRandom random) throws InvalidKeyException {
        if (key == null) {
            throw new InvalidKeyException("key is null");
        }
        if (!(key instanceof SecretKey)) {
            throw new InvalidKeyException("Key is not a secret key");
        }

        this.mode = opmode;
        if (mode != Cipher.ENCRYPT_MODE && mode != Cipher.DECRYPT_MODE) {
            throw new UnsupportedOperationException("Cipher mode not supported");
        }

        try {
            synchronized (aesWriteLock) {
                if (this.aes == null) {
                    this.aes = Cipher.getInstance("AES/CBC/PKCS5Padding");
                }
            }
            this.key = (SecretKey) key;
        } catch (NoSuchPaddingException | NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Initializes this cipher with a key, a set of
     * algorithm parameters, and a source of randomness.
     *
     * <p>Note that when a Cipher object is initialized, it loses all
     * previously-acquired state. In other words, initializing a Cipher is
     * equivalent to creating a new instance of that Cipher and initializing
     * it.
     *
     * @param opmode the operation mode of this cipher (this is one of
     *               the following:
     *               <code>ENCRYPT_MODE</code>, <code>DECRYPT_MODE</code>,
     *               <code>WRAP_MODE</code> or <code>UNWRAP_MODE</code>)
     * @param key    the encryption key
     * @param params the algorithm parameters
     * @param random the source of randomness
     * @throws InvalidKeyException                if the given key is inappropriate for
     *                                            initializing this cipher
     * @throws InvalidAlgorithmParameterException if the given algorithm
     *                                            parameters are inappropriate for this cipher,
     *                                            or if this cipher requires
     *                                            algorithm parameters and <code>params</code> is null.
     * @throws UnsupportedOperationException      if {@code opmode} is
     *                                            {@code WRAP_MODE} or {@code UNWRAP_MODE} is not implemented
     *                                            by the cipher.
     */
    @Override
    protected void engineInit(int opmode, Key key, AlgorithmParameterSpec params, SecureRandom random)
            throws InvalidKeyException, InvalidAlgorithmParameterException {
        engineInit(opmode, key, random);
    }

    /**
     * Initializes this cipher with a key, a set of
     * algorithm parameters, and a source of randomness.
     *
     * <p>Note that when a Cipher object is initialized, it loses all
     * previously-acquired state. In other words, initializing a Cipher is
     * equivalent to creating a new instance of that Cipher and initializing
     * it.
     *
     * @param opmode the operation mode of this cipher (this is one of
     *               the following:
     *               <code>ENCRYPT_MODE</code>, <code>DECRYPT_MODE</code>,
     *               <code>WRAP_MODE</code> or <code>UNWRAP_MODE</code>)
     * @param key    the encryption key
     * @param params the algorithm parameters
     * @param random the source of randomness
     * @throws InvalidKeyException                if the given key is inappropriate for
     *                                            initializing this cipher
     * @throws InvalidAlgorithmParameterException if the given algorithm
     *                                            parameters are inappropriate for this cipher,
     *                                            or if this cipher requires
     *                                            algorithm parameters and <code>params</code> is null.
     * @throws UnsupportedOperationException      if {@code opmode} is
     *                                            {@code WRAP_MODE} or {@code UNWRAP_MODE} is not implemented
     *                                            by the cipher.
     */
    @Override
    protected void engineInit(int opmode, Key key, AlgorithmParameters params, SecureRandom random)
            throws InvalidKeyException, InvalidAlgorithmParameterException {
        engineInit(opmode, key, random);
    }

    /**
     * Continues a multiple-part encryption or decryption operation
     * (depending on how this cipher was initialized), processing another data
     * part.
     *
     * <p>The first <code>inputLen</code> bytes in the <code>input</code>
     * buffer, starting at <code>inputOffset</code> inclusive, are processed,
     * and the result is stored in a new buffer.
     *
     * @param input       the input buffer
     * @param inputOffset the offset in <code>input</code> where the input
     *                    starts
     * @param inputLen    the input length
     * @return the new buffer with the result, or null if the underlying
     * cipher is a block cipher and the input data is too short to result in a
     * new block.
     */
    @Override
    protected byte[] engineUpdate(byte[] input, int inputOffset, int inputLen) {
        throw new UnsupportedOperationException();
    }

    /**
     * Continues a multiple-part encryption or decryption operation
     * (depending on how this cipher was initialized), processing another data
     * part.
     *
     * <p>The first <code>inputLen</code> bytes in the <code>input</code>
     * buffer, starting at <code>inputOffset</code> inclusive, are processed,
     * and the result is stored in the <code>output</code> buffer, starting at
     * <code>outputOffset</code> inclusive.
     *
     * <p>If the <code>output</code> buffer is too small to hold the result,
     * a <code>ShortBufferException</code> is thrown.
     *
     * @param input        the input buffer
     * @param inputOffset  the offset in <code>input</code> where the input
     *                     starts
     * @param inputLen     the input length
     * @param output       the buffer for the result
     * @param outputOffset the offset in <code>output</code> where the result
     *                     is stored
     * @return the number of bytes stored in <code>output</code>
     * @throws ShortBufferException if the given output buffer is too small
     *                              to hold the result
     */
    @Override
    protected int engineUpdate(byte[] input, int inputOffset, int inputLen, byte[] output, int outputOffset) throws ShortBufferException {
        throw new UnsupportedOperationException();
    }

    /**
     * Encrypts or decrypts data in a single-part operation,
     * or finishes a multiple-part operation.
     * The data is encrypted or decrypted, depending on how this cipher was
     * initialized.
     *
     * <p>The first <code>inputLen</code> bytes in the <code>input</code>
     * buffer, starting at <code>inputOffset</code> inclusive, and any input
     * bytes that may have been buffered during a previous <code>update</code>
     * operation, are processed, with padding (if requested) being applied.
     * The result is stored in a new buffer.
     *
     * <p>Upon finishing, this method resets this cipher object to the state
     * it was in when previously initialized via a call to
     * <code>engineInit</code>.
     * That is, the object is reset and available to encrypt or decrypt
     * (depending on the operation mode that was specified in the call to
     * <code>engineInit</code>) more data.
     *
     * @param input       the input buffer
     * @param inputOffset the offset in <code>input</code> where the input
     *                    starts
     * @param inputLen    the input length
     * @return the new buffer with the result
     * @throws IllegalBlockSizeException if this cipher is a block cipher,
     *                                   no padding has been requested (only in encryption mode), and the total
     *                                   input length of the data processed by this cipher is not a multiple of
     *                                   block size; or if this encryption algorithm is unable to
     *                                   process the input data provided.
     * @throws BadPaddingException       if this cipher is in decryption mode,
     *                                   and (un)padding has been requested, but the decrypted data is not
     *                                   bounded by the appropriate padding bytes
     */
    @Override
    protected byte[] engineDoFinal(byte[] input, int inputOffset, int inputLen) throws IllegalBlockSizeException, BadPaddingException {
        if (mode == Cipher.DECRYPT_MODE) {
            return doDecrypt(input);
        }
        else if (mode == Cipher.ENCRYPT_MODE) {
            return doEncrypt(input);
        }

        return new byte[0];
    }

    /**
     * Encrypts or decrypts data in a single-part operation,
     * or finishes a multiple-part operation.
     * The data is encrypted or decrypted, depending on how this cipher was
     * initialized.
     *
     * <p>The first <code>inputLen</code> bytes in the <code>input</code>
     * buffer, starting at <code>inputOffset</code> inclusive, and any input
     * bytes that may have been buffered during a previous <code>update</code>
     * operation, are processed, with padding (if requested) being applied.
     * The result is stored in the <code>output</code> buffer, starting at
     * <code>outputOffset</code> inclusive.
     *
     * <p>If the <code>output</code> buffer is too small to hold the result,
     * a <code>ShortBufferException</code> is thrown.
     *
     * <p>Upon finishing, this method resets this cipher object to the state
     * it was in when previously initialized via a call to
     * <code>engineInit</code>.
     * That is, the object is reset and available to encrypt or decrypt
     * (depending on the operation mode that was specified in the call to
     * <code>engineInit</code>) more data.
     *
     * <p>Note: if any exception is thrown, this cipher object may need to
     * be reset before it can be used again.
     *
     * @param input        the input buffer
     * @param inputOffset  the offset in <code>input</code> where the input
     *                     starts
     * @param inputLen     the input length
     * @param output       the buffer for the result
     * @param outputOffset the offset in <code>output</code> where the result
     *                     is stored
     * @return the number of bytes stored in <code>output</code>
     * @throws IllegalBlockSizeException if this cipher is a block cipher,
     *                                   no padding has been requested (only in encryption mode), and the total
     *                                   input length of the data processed by this cipher is not a multiple of
     *                                   block size; or if this encryption algorithm is unable to
     *                                   process the input data provided.
     * @throws ShortBufferException      if the given output buffer is too small
     *                                   to hold the result
     * @throws BadPaddingException       if this cipher is in decryption mode,
     *                                   and (un)padding has been requested, but the decrypted data is not
     *                                   bounded by the appropriate padding bytes
     */
    @Override
    protected int engineDoFinal(byte[] input, int inputOffset, int inputLen, byte[] output, int outputOffset) throws ShortBufferException, IllegalBlockSizeException, BadPaddingException {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns the configured AES-{@link Cipher} instance.
     *
     * @return the configured AES-{@link Cipher} instance.
     */
    public Cipher getAESCipher() {
        synchronized (aesWriteLock) {
            return aes;
        }
    }

}

