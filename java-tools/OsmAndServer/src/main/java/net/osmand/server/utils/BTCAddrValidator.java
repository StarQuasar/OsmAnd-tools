package net.osmand.server.utils;

import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/*
    Source: https://github.com/timqi/btc_address_validator
 */
public class BTCAddrValidator {

    private static final char[] ALPHABET = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz".toCharArray();

    private static final int[] INDEXES = new int[128];

    private static final MessageDigest digest;

    static {
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        for (int i = 0; i < INDEXES.length; i++) {
            INDEXES[i] = -1;
        }
        for (int i = 0; i < ALPHABET.length; i++) {
            INDEXES[ALPHABET[i]] = i;
        }
    }
    
    public static void main(String[] args) {
    	System.out.println(validate("bc1qs5q679tac0uvfunt0gdwuymves5re7v7q8fntv"));
		System.out.println(validate("3LrXizKejCGYyGUxYzGweyuxFVtfs3odEe"));
	}

    public static boolean validate(String addr) {
        try {
            int addressHeader = getAddressHeader(addr);
            return (addressHeader == 0 || addressHeader == 5);
        } catch (Exception x1) {
        	try {
				if (Bech32.decode(addr).hrp != null) {
					return true;
				}
        	} catch (Exception x) {
        		x.printStackTrace();
        	}
        }
        return false;
    }

    private static int getAddressHeader(String address) throws IOException {
        byte[] tmp = decodeChecked(address);
        return tmp[0] & 0xFF;
    }

    private static byte[] decodeChecked(String input) throws IOException {
        byte[] tmp = decode(input);
        if (tmp.length < 4)
            throw new IOException("BTC AddressFormatException Input too short");
        byte[] bytes = copyOfRange(tmp, 0, tmp.length - 4);
        byte[] checksum = copyOfRange(tmp, tmp.length - 4, tmp.length);

        tmp = doubleDigest(bytes);
        byte[] hash = copyOfRange(tmp, 0, 4);
        if (!Arrays.equals(checksum, hash))
            throw new IOException("BTC AddressFormatException Checksum does not validate");

        return bytes;
    }

    private static byte[] doubleDigest(byte[] input) {
        return doubleDigest(input, 0, input.length);
    }

    private static byte[] doubleDigest(byte[] input, int offset, int length) {
        synchronized (digest) {
            digest.reset();
            digest.update(input, offset, length);
            byte[] first = digest.digest();
            return digest.digest(first);
        }
    }

    private static byte[] decode(String input) throws IOException {
        if (input.length() == 0) {
            return new byte[0];
        }
        byte[] input58 = new byte[input.length()];
        // Transform the String to a base58 byte sequence
        for (int i = 0; i < input.length(); ++i) {
            char c = input.charAt(i);
            int digit58 = -1;
            if (c >= 0 && c < 128) {
                digit58 = INDEXES[c];
            }
            if (digit58 < 0) {
                throw new IOException("Bitcoin AddressFormatException Illegal character " + c + " at " + i);
            }

            input58[i] = (byte) digit58;
        }

        // Count leading zeroes
        int zeroCount = 0;
        while (zeroCount < input58.length && input58[zeroCount] == 0) {
            ++zeroCount;
        }
        // The encoding
        byte[] temp = new byte[input.length()];
        int j = temp.length;

        int startAt = zeroCount;
        while (startAt < input58.length) {
            byte mod = divmod256(input58, startAt);
            if (input58[startAt] == 0) {
                ++startAt;
            }

            temp[--j] = mod;
        }
        // Do no add extra leading zeroes, move j to first non null byte.
        while (j < temp.length && temp[j] == 0) {
            ++j;
        }

        return copyOfRange(temp, j - zeroCount, temp.length);
    }

    private static byte divmod256(byte[] number58, int startAt) {
        int remainder = 0;
        for (int i = startAt; i < number58.length; i++) {
            int digit58 = (int) number58[i] & 0xFF;
            int temp = remainder * 58 + digit58;

            number58[i] = (byte) (temp / 256);

            remainder = temp % 256;
        }
        return (byte) remainder;
    }

    private static byte[] copyOfRange(byte[] source, int from, int to) {
        byte[] range = new byte[to - from];
        System.arraycopy(source, from, range, 0, range.length);
        return range;
    }
}
