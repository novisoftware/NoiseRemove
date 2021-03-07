package com.github.novisoftware.noiseRemove;

/**
 *
 *
 * @author Shiro SADO (sh.sado@gmail.com)
 *
 */
abstract class ByteData {
	abstract int getValue(int ch, int index);
	abstract void setValue(int ch, int index, int value);

	static class ByteData16 extends ByteData {
		private final byte[] data;
		private final int nChannels;
		private final int byte0;
		private final int byte1;

		ByteData16(byte[] data, int nChannels, boolean isBigEndian) {
			this.data = data;
			this.nChannels = nChannels;
			this.byte0 = isBigEndian ? 0 : 1;
			this.byte1 = isBigEndian ? 1 : 0;
		}

		int getValue(int ch, int index) {
			return
					(short)
					((data[index*2*nChannels + ch*2 + byte0] <<8) +
			         (data[index*2*nChannels + ch*2 + byte1] & 0xFF ));
		}

		void setValue(int ch, int index, int value) {
			data[index*2*nChannels + ch*2 + byte0] = (byte)(value >> 8);
			data[index*2*nChannels + ch*2 + byte1] = (byte)(value);
		}
	}

	static class ByteData24 extends ByteData {
		private final byte[] data;
		private final int nChannels;
		private final int byte0;
		private final int byte1;
		private final int byte2;


		ByteData24(byte[] data, int nChannels, boolean isBigEndian) {
			this.data = data;
			this.nChannels = nChannels;
			this.byte0 = isBigEndian ? 0 : 2;
			this.byte1 = isBigEndian ? 1 : 1;
			this.byte2 = isBigEndian ? 2 : 0;
		}

		int getValue(int ch, int index) {
			return
					((data[index*3*nChannels + ch*3 + byte0]       ) <<16) +
					((data[index*3*nChannels + ch*3 + byte1] & 0xFF) <<8) +
					((data[index*3*nChannels + ch*3 + byte2] & 0xFF ));
		}

		void setValue(int ch, int index, int value) {
			data[index*3*nChannels + ch*3 + byte0] = (byte)(value >> 16);
			data[index*3*nChannels + ch*3 + byte1] = (byte)(value >> 8);
			data[index*3*nChannels + ch*3 + byte2] = (byte)(value);
		}
	}

}

