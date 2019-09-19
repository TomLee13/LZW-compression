package edu.cmu.andrew.mingyan2;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/*
 * There are 2 ways to run this program.
 * 1. Run in cmd or terminal. However, please be sure to put all the source file into src/ folder.
 * 2. Use run configurations in Eclipse and type arguments for this program.
 */

public class LZWCompression {

	private static byte leftBuff; // used to store the 4 meaningful bits in the following operation
	private static boolean buffFull; // if true, there's meaningful 4 bits stored in leftBuff.
	private static int numBytesRead = 0;
	private static int numBytesWritten = 0;

	private static void LZW_Compress(String inputFile, String outputFile) throws IOException {
		// System.out.println("LZW_Compress start");
		DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(inputFile)));
		DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(outputFile)));
		buffFull = false;
		// build hashMap
		HashMap table = new HashMap();
		StringBuilder buffer = new StringBuilder();
		char c = read(in);
		// System.out.println("char is " + (int) c);
		buffer.append(c);
		String s, sc;
		s = buffer.toString();
		int key;
		try {
			while (true) {
				c = read(in);
				// System.out.println("char is " + (int) c);
				s = buffer.toString();
				buffer.append(c);
				sc = buffer.toString();
				// System.out.println("buffer is " + bufferString);
				key = table.find(sc);
				if (key >= 0) { // in the table

				} else {// not found
					// output codeword(s);
					key = table.find(s);
					write(out, key);
					// Enter s + c into the table;
					table.put(sc);
					// reset buffer
					buffer = new StringBuilder();
					buffer.append(c);
				}
			}
		} catch (EOFException e) {
			in.close();
		}

		// output codeword(s);
		s = buffer.toString();
		key = table.find(s);
		write(out, key);
		flush(out);
		// System.out.println("write key=" + key);

		out.close();

		// System.out.println("\nLZW_Compress done");
	}

	// read the in file byte by byte
	private static char read(DataInputStream in) throws IOException {
		byte byteIn;
		byteIn = in.readByte();
		char c = (char) byteIn;
		c = (char) (byteIn & 0xFF);
		numBytesRead++;
		return c;
	}

	// write the file out byte by byte.
	// if there's no bits in leftbuff, just write the left 8 meaningful bits to out
	// file and store the last 4 bits to leftbuff
	// if there is bits in leftbuff left, write the next 4 bits of the next 12 bits
	// to the out file
	private static void write(DataOutputStream out, int i) throws IOException {
		// i=00..0,1111,1111,1111
		byte[] buffer2 = toByte(i);
		byte buffer1;
		if (buffFull) {
			// System.out.println("full");
			// clear buff
			buffer1 = leftBuff; // 0000,1111
			buffer1 <<= 4; // only 4 bit is meaningful // 1111,0000
			buffer1 |= (buffer2[0] >> 4) & 0xF; // add first 4 bit 1111,0000+0000,1111
			out.write(buffer1);
			numBytesWritten++;

			// write next 2 byte
			buffer1 = buffer2[0]; // 1111,1111
			buffer1 <<= 4; // add last 4 bit 1111,0000
			buffer1 |= buffer2[1]; // add only 4 bit 1111,0000+0000,1111
			out.write(buffer1);
			numBytesWritten++;
			buffFull = false;
		} else {
			// System.out.println("empty");
			// write first byte
			buffer1 = buffer2[0]; // 8 bit 1111,1111
			out.write(buffer1);
			numBytesWritten++;
			// store second in buff
			leftBuff = buffer2[1]; // 0000,1111
			buffFull = true;
		}
	}

	// if write() method is done and there's still bits left in left buff, flush
	// them out to the out file.
	private static void flush(DataOutputStream out) throws IOException {

		byte buffer1;
		if (buffFull) {
			// System.out.println("full");
			// clear buff
			buffer1 = leftBuff; // 0000,1111
			buffer1 <<= 4; // only 4 bit is meaningful // 1111,0000
			out.write(buffer1);
			numBytesWritten++;
		}
		// System.out.println("flush empty total bytes=" + num_bytes_written);
	}

	/*
	 * convert a int to a meaningful 12-bit buffer. this buffer is a Byte array of
	 * length 2. buffer[0] contains the first 8 meaningful bits. buffer[1] contains
	 * the last 4 meaningful bits.
	 */
	private static byte[] toByte(int i) {
		byte[] buffer = new byte[] { 0, 0 };// 16 bit i=00..0,1111,1111,1111
		buffer[0] = (byte) ((i >> 4) & 0xFF); // 8 bit 1111,1111
		buffer[1] = (byte) (i & 0xF); // 8 bit only 4 is meaningful 0000,1111
		return buffer;
	}

	// the logic here is very similar to write() method
	private static int toInt(byte[] bytes) {
		int ret = 0;
		if (buffFull) {
			ret = leftBuff; // 0000,0000,1111
			ret = ret << 8; // 1111,0000,0000
			ret |= (int) (bytes[0] & 0xFF); // 1111,0000,0000+1111,1111
			buffFull = false;

		} else {
			ret = bytes[0]; // 00..0,0000,1111,1111
			ret = ((ret << 4) & 0xFF0); // 00..0,1111,1111,0000
			ret |= (int) ((bytes[1] >> 4) & 0xF); // 1111,1111,0000+0000,1111
			leftBuff = (byte) (bytes[1] & 0xF); // 0000,1111
			buffFull = true;
		}

		// System.out.println("int is " + ret);
		return ret;
	}

	// read 1 or 2 bytes and convert them to 12-bit unit.
	private static int readInt(DataInputStream in) throws IOException {
		byte[] bytes = new byte[] { 0, 0 };
		int endIn = bytes.length;// 2
		if (buffFull)
			endIn = 1;
		int numRead = in.read(bytes, 0, endIn);

		// System.out.println("num_read is " + num_read);
		if (numRead <= 0)
			throw new EOFException("end of file");

		numBytesRead += numRead;
		return toInt(bytes);
	}

	// write byte by byte to the out file
	private static void writeString(DataOutputStream out, String s) throws IOException {
		char c;
		byte buff;
		for (int i = 0; i < s.length(); i++) {
			c = s.charAt(i); // 0000,0000,1111,1111
			buff = (byte) (c & 0xFF); // 1111,1111
			out.writeByte(buff);
			numBytesWritten++;
		}
	}

	private static void LZW_Decompress(String inputFile, String outputFile) throws IOException {

		// System.out.println("LZW_Decompress start");
		DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(inputFile)));
		DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(outputFile)));
		buffFull = false;
		// build hashMap
		HashMap table = new HashMap();
		int key;
		String s, priorCodeWord, outS;
		key = readInt(in);
		// System.out.println("key is " + key);
		s = table.getValue(key);
		priorCodeWord = s;
		// System.out.println("s is " + s);
		writeString(out, s);
		try {
			while (true) {
				key = readInt(in);
				// System.out.println("key is " + key);
				s = table.getValue(key);
				if (s == null) {// codeword not in the table
					outS = priorCodeWord + priorCodeWord.charAt(0);
					table.put(outS);
					// System.out.println("s is " + outS);
					s = outS;
					writeString(out, s);
				} else {
					key = table.put(priorCodeWord + s.charAt(0));
					// System.out.println("s is " + s);
					writeString(out, s);
				}
				priorCodeWord = s;
				// System.out.println("pc is " + priorCodeWord);
			}
		} catch (EOFException e) {
			in.close();
		}

		out.close();
		// System.out.println("LZW_Decompress done");
	}

	// My program can handle both binary and ASCII file.
	// In all the file-in and file-out operation, I read the file byte by byte.
	// words.html: degree of compression = 1.1MB/2.5MB * 100% = 44%
	// CrimeLatLonXY.csv: degree of compression = 1.3MB/2.6MB * 100% = 50%
	// 01_Overview.mp4: degree of compression = 33.8MB/25MB * 100% = 135.2%
	public static void main(String args[]) {
		// if the user only type "-c" or "-d" and the filename of the source and zipped
		// or unzipped file
		if (args.length == 3) {
			if (args[0].equals("-c")) {
				try {
					LZW_Compress(args[1], args[2]);
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else if (args[0].equals("-d")) {
				try {
					LZW_Decompress(args[1], args[2]);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} else if (args.length == 4) {
			if ((args[0].equals("-c")) && (args[1].equals("-v"))) {
				try {
					LZW_Compress(args[2], args[3]);
					System.out.println("bytes read = " + numBytesRead + " , bytes written = " + numBytesWritten);
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else if ((args[0].equals("-d")) && (args[1].equals("-v"))) {
				try {
					LZW_Decompress(args[2], args[3]);
					System.out.println("bytes read = " + numBytesRead + " , bytes written = " + numBytesWritten);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}

}
