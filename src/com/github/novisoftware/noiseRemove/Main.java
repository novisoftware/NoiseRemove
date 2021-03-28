package com.github.novisoftware.noiseRemove;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;

import com.github.novisoftware.noiseRemove.ByteData.ByteData16;
import com.github.novisoftware.noiseRemove.ByteData.ByteData24;

/**
 *
 *
 * @author Shiro SADO (sh.sado@gmail.com)
 *
 */
public class Main {
	final byte[] data;

	/**
	 * Read DWORD in RIFF header.
	 * RIFFヘッダのダブルワード読み取り用。
	 *
	 * @param b
	 * @param start
	 * @return
	 */
	static int b2dword(byte b[], int start) {
		return
				  (0xff & b[3 + start]) * 0x1000000
				+ (0xff & b[2 + start]) * 0x10000
				+ (0xff & b[1 + start]) * 0x100
				+ (0xff & b[0 + start]);
	}

	/**
	 * Read WORD in RIFF header.
	 * RIFFヘッダのワード読み取り用。
	 *
	 * @param b
	 * @param start
	 * @return
	 */
	static int b2word(byte b[], int start) {
		return
				  (0xff & b[1 + start]) * 0x100
				+ (0xff & b[0 + start]);
	}

	/**
	 * Extract 4 chars (use in debugging).
	 * デバッグ表示の用途で、RIFFヘッダ中の文字列(4文字)を抽出する。
	 *
	 * @param b
	 * @param start
	 * @return
	 */
	static String b2idt(byte b[], int start) {
		StringBuilder sb = new StringBuilder();
		for (int i=0; i <4; i++) {
			if (0x20 <= b[i+start] && b[i+start] < 0x7e ) {
				sb.append( String.format("%c", b[i+start]) );
			}
		}

		return sb.toString();
	}

	/**
	 * format check.
	 * フォーマットの点検をする。
	 *
	 * @param s
	 * @param b
	 * @param start
	 */
	static void checkFormat(String s, byte b[], int start) {
		for (int i=0 ; i < s.length(); i++) {
			if (s.charAt(i) != b[i + start]) {
				StringBuilder sb = new StringBuilder();
				for (int j=0; j < s.length(); j++) {
					if (0x20 <= b[j+start] && b[j+start] < 0x7e ) {
						sb.append( String.format("%c", b[j+start]) );
					}
				}
				System.out.println( "format check error(expected:" + s + " actually:" + sb.toString() +  ").");
				System.exit(1);
			}
		}
	}

	final int nChannels;
	final int nSamplesPerSec;
	final int wBitsPerSample;
	final boolean isBigEndian = false;
	final int startOffset;
	final int INT_SCALE;
	final int BYTE_LENGTH;

	/**
	 * Audio data.
	 * 音声データの読み取り用。
	 */
	final ByteData byteData;

	Main(String inputFile) throws Exception {
	      DataInputStream inputStream =
	          new DataInputStream(
	              new BufferedInputStream(
	                  new FileInputStream(inputFile)));

	      byte[] b = new byte[200];
	      inputStream.read(b);

	      // ダンプ表示
	      for (int i=0 ; i<b.length ; i++) {
	    	  String c = " ";
	    	  if (0x20 < b[i] && b[i] < 0x7F) {
	    		  c = String.format("%c", b[i]);
	    	  }
	    	  System.out.print( String.format(" %02x(%s) ", b[i], c) );
	    	  if( i% 16 == 15) {
	    		  System.out.println();
	    	  }
	      }
		  System.out.println();

	      checkFormat("RIFF", b, 0);
	      checkFormat("WAVE", b, 8);
	      checkFormat("fmt", b, 12);

	      int size = b2dword(b, 4);
	      int formatChunkSize = b2dword(b, 16);
	      int wFormatTag = b2word( b, 20 );
	      int nChannels = b2word( b, 22 );
	      int nSamplesPerSec = b2dword( b, 24 );
	      int nAvgBytesPerSec = b2dword( b, 28 );
	      int nBlockAlign = b2word( b, 32 );
	      int wBitsPerSample = b2word( b, 34 );
	      int cbSize = b2word( b, 36 );

	      NumberFormat comma = NumberFormat.getNumberInstance();
	      System.out.println( "size1: " + comma.format(size));
	      System.out.println( "fmt chunk: size = " + comma.format(formatChunkSize));
	      System.out.println( "wFormatTag = " + wFormatTag);
	      System.out.println( "nChannels = " + nChannels);
	      System.out.println( "nSamplesPerSec = " + nSamplesPerSec);
	      System.out.println( "nAvgBytesPerSec = " + nAvgBytesPerSec);
	      System.out.println( "nBlockAlign = " + nBlockAlign);
	      System.out.println( "wBitsPerSample = " + wBitsPerSample);
	      System.out.println( "cbSize = " + cbSize);

	      this.nChannels = nChannels;
	      this.nSamplesPerSec = nSamplesPerSec;
	      this.wBitsPerSample = wBitsPerSample;

	      int dataSize = 0;
	      int startOffset = 0;

	      String idt = b2idt(b, 20 + formatChunkSize);
	      System.out.println( "chunk: " + idt );
	      if (idt.equals( "data" )) {
	    	  startOffset = 24 + formatChunkSize;
	    	  dataSize = b2dword( b, startOffset);
		      System.out.println( "dataSize = " + comma.format(dataSize));
	    	  startOffset += 4;
	      }
	      else  if (idt.equals( "LIST" )) {
		      int listSize = b2word( b, 24 + formatChunkSize);
		      System.out.println( "listSize = " + listSize);
		      System.out.println();

	    	  startOffset = 20 + formatChunkSize + 8 + listSize;
		      checkFormat("data", b, startOffset);
	    	  startOffset += 4;
	    	  dataSize = b2dword( b, startOffset);
	    	  startOffset += 4;
		      System.out.println( "dataSize = " + comma.format(dataSize));
	      }
	      else {
	    	  System.err.println("Format check failed. Found unknown chunk (" + idt + ").");
	    	  System.exit(1);
	      }
	      inputStream.close();

	      DataInputStream dataInStream2 =
		          new DataInputStream(
		              new BufferedInputStream(
		                  new FileInputStream(inputFile)));

	      System.out.println("startOffset: " + startOffset);
	      System.out.println("dataSize: " + dataSize);

	      data = new byte[dataSize];
	      dataInStream2.skip(startOffset);
	      dataInStream2.read(data);

	      dataInStream2.close();

	      this.startOffset = startOffset;

	      if (this.wBitsPerSample == 16) {
		      byteData = new ByteData16(data, nChannels, isBigEndian);
		      INT_SCALE = 1;
		      BYTE_LENGTH = 2;
	      }
	      else if(this.wBitsPerSample == 24) {
	    	  byteData = new ByteData24(data, nChannels, isBigEndian);
		      INT_SCALE = 256;
		      BYTE_LENGTH = 3;
	      }
	      else {
	    	  throw new Exception(String.format("Unsupport format(wBitsPerSample=%d).", wBitsPerSample));
	      }
	}

	int abs(int x) {
		return x < 0 ? -x : x;
	}

	class FoundNoise {
		int ch;
		int start;
		int length;
	}

	class WorkNoiseMemo {
		int time;
		int size;

		WorkNoiseMemo(int time, int size) {
			this.time = time;
			this.size = size;
		}
	}

	double ddSum(int[] dataRing) {
		double sum = 0;

		for(int i = 0; i < dataRing.length; i++) {
			sum += dataRing[i];
		}

		return sum / dataRing.length;
	}


	FoundNoise aggregate(int totalThreshold, int[] dataRing, int ch, ArrayList<WorkNoiseMemo> memo, int length_adder) {
		double t = 0;
		double w = 0;
		int tMin = 0;
		int tMax = 0;
		boolean isFirst = true;

		for (WorkNoiseMemo m : memo) {
			t += (double)m.time * m.size;
			w += m.size;

			if (isFirst) {
				isFirst = false;

				tMin = m.time;
				tMax = m.time;
			}
			else {
				if (m.time < tMin) {
					tMin = m.time;
				}
				if (tMax < m.time) {
					tMax = m.time;
				}
			}
		}

		// 周囲の N倍に満たなかったら、ノイズとして判定しない
		if (w/memo.size() < 5 * ddSum(dataRing)) {
			return null;
		}

//		double weightThreashold = 400;
		if (w < totalThreshold) {
			return null;
		}

		FoundNoise n = new FoundNoise();
		/*
		n.start = tMin;
		n.length = tMax - tMin + 1;
		*/

		int t_center = (int) Math.round(t/w);
		n.length = (tMax - tMin + 1) * 3 / 2  + length_adder;
		n.start = t_center - n.length/2;
		n.ch = ch;

		return n;
	}

	ArrayList<FoundNoise> noiseList;



	// final int th = 500;
	// final double max_noiseContinueSec = 0.0005;
	void check(final int threshold, final int totalThreshold, final double max_noiseContinueSec, int length_adder) {
		final int gap = 10;
		final int DEBUG_FOUND_N = 200;

		int[] dataRing = new int[4000];
		int[] dataRing2 = new int[4000];

		noiseList = new ArrayList<>();

		boolean isFirst = true;
		for (int ch = 0; ch < nChannels ; ch++) {
			ArrayList<WorkNoiseMemo> memo = new ArrayList<>();
			int printCount = 0;
			double noiseStartSec = 0;


			for(int index = 0 + gap  + nSamplesPerSec ; index < data.length / BYTE_LENGTH / nChannels - gap   ; index++) {
				try {
					int value0 = this.byteData.getValue(ch, index);
					int value1 = this.byteData.getValue(ch, index-1);
					int value2 = this.byteData.getValue(ch, index-2);
					int dy0 = value1 - value0;
					int dy1 = value2 - value1;

					int ddy = abs(dy1 - dy0);

					dataRing2[index % dataRing2.length] = dataRing[index % dataRing.length];
					dataRing[index % dataRing.length] = ddy; // this.byteData.getData(ch, index-envrionmentGap);

					if (ddy > threshold * INT_SCALE) {
						double sec = (double)index /  nSamplesPerSec;
						if (noiseStartSec + max_noiseContinueSec < sec) {
							// 一定以上の場合は、スクラッチノイズのカットをしない
							if (ddSum(dataRing) < 1000 * INT_SCALE) {
								FoundNoise w = aggregate(totalThreshold, dataRing2, ch, memo, length_adder);

								// 集計結果が閾値を超えて、ノイズと判定した場合
								if (w != null) {
									noiseList.add(w);

									if (printCount++ < 30 ) {
										System.out.printf("     ch[%d]      ST  %10d LEN %4d   ST(s) %9.5f LEN(s) %9.5f \n",
												ch, w.start, w.length,
												(double)w.start / nSamplesPerSec,
												(double)w.length / nSamplesPerSec
												);
									}
									/*
									if (printCount > 2000) {
										return;
									}
									*/
								}
							}
							noiseStartSec = sec;
							memo = new ArrayList<>();
						}

						memo.add(new WorkNoiseMemo(index, ddy));

						if (memo.size() > DEBUG_FOUND_N) {
							return;
						}


						/*
						// 表示
						if (isFirst) {
							isFirst = false;
							System.out.println("ch sec ");
						}
						if (printCount++ < 300 ) {
							System.out.printf( " %d %9.5f %10d %5d  %d\n", ch, sec, idx,
									abs(value - value0),
									value - value0b );
						}
						*/
					}
				} catch(ArrayIndexOutOfBoundsException ex) {
					System.out.println("index = " + index);

					throw ex;
				}
			}
		}
	}

	/**
	 * 波形の線形補間（無音化）による修復
	 */
	void repair() {
		int ch0cnt = 0;
		int ch1cnt = 0;

		int dmy = 0;

		for(FoundNoise n : noiseList) {
			try {
				int ch = n.ch;
				int idx0 = n.start - n.length;
				int idx1 = n.start + 2 * n.length;
				int value0 = this.byteData.getValue(ch, idx0);
				int value1 = this.byteData.getValue(ch, idx1);

				for (int i = idx0 + 1 ; i < idx1 ; i++) {
					int repairValue = (int)
							Math.round(
									(double)(value1 - value0) * (i - idx0) / (idx1  - idx0) + value0
									);

					/*
					if (dmy++ < 150) {
						System.out.println("idx1: " + i +  "    t:" + String.format("%.9f",  i / 96.0 / 1000)  + "    repair value: " + repairValue + "   old value: " + this.byteData.getValue(ch, i));
					}
					*/

					this.byteData.setValue(ch, i, repairValue);
					/*
					if (dmy++ < 150) {
						System.out.println("idx2: " + i +  "    t:" + String.format("%.9f",  i / 96.0 / 1000)  + "    repair value: " + repairValue + "   old value: " + this.byteData.getValue(ch, i));
					}
					*/

				}

				// 表示用集計データ
				if (ch == 0) {
					ch0cnt ++;
				}
				if (ch == 1) {
					ch1cnt ++;
				}
			} catch(ArrayIndexOutOfBoundsException e) {
			}
		}

		System.out.println("ch0: " + ch0cnt + "   ch1: " + ch1cnt);
	}

	private final double square(double x) {
		return x * x;
	}

	private final double compress1(double a) {
//		final double factor1 = 500;
		final double factor1 = 50;

		if (a > factor1) {
			return factor1 * Math.sqrt(a / factor1);
		}
		if (a < -factor1) {
			return - factor1 * Math.sqrt(-a / factor1);
		}

		return a;
	}

	// 弱音化による修復
	void repair_mute(int ch, int idx0, int idx1) {
		int length = idx1 - idx0 + 1;

		// 1階微分フィルタ
		int[] d = new int[length];
		int oldValue = 0;
		for (int i = 0 ; i < length + 1 ; i++) {
			int v = this.byteData.getValue(ch, idx0);
			if (i != 0) {
				d[i - 1] = oldValue - v;
			}
			oldValue = v;
		}

		// 2階微分
		double[] dd = new double[length];
		for (int i = 0 ; i < length-1; i++) {
			dd[i+1] = d[i + 1] - d[i];
		}

		// 2階微分値を加工
		for (int i = 0; i < length; i++) {
			dd[i] = compress1(dd[i]);
		}

		/*
		// 整数化
		int[] dd_int = new int[length];
		for (int i = 0; i < length; i++) {
			dd_int[i] = (int) Math.round(dd[i]);
		}
		*/

		/*
		// 合算
		double dd_int_sum = 0;
		double work_d = 0;
		for (int i = 0; i < length; i++) {
			work_d += dd[i];

			dd_int_sum += dd_int[i];
		}
		*/

		int value0 = this.byteData.getValue(ch, idx0);
		int value1 = this.byteData.getValue(ch, idx1);

		double work_d = dd[0];
		double work_val = 0;

		System.out.println("repair: " + idx0 + " " + idx1);

		for (int i = idx0 + 1 ; i < idx1 ; i++) {
			work_d += dd[i - idx0];
			work_val += work_d;

			int repairValue = (int)
					Math.round(
							(double)(value1 - value0) * (i - idx0) / (idx1  - idx0)  + value0 + work_val
							);

			this.byteData.setValue(ch, i, repairValue);
		}
	}



	// 無音化による修復
	void repair_remove(int ch, int idx0, int idx1) {
		int value0 = this.byteData.getValue(ch, idx0);
		int value1 = this.byteData.getValue(ch, idx1);

		for (int i = idx0 + 1 ; i < idx1 ; i++) {
			int repairValue = (int)
					Math.round(
							(double)(value1 - value0) * (i - idx0) / (idx1  - idx0) + value0
							);

			this.byteData.setValue(ch, i, repairValue);
		}
	}

	// コピーによる修復
	void repair_a_point(int ch, int idx0, int idx1) {
		final int REPAIR_BUFF_LEN = 1200;
		final int AC_WINDOW = 600;

		int value0 = this.byteData.getValue(ch, idx0);
		int value1 = this.byteData.getValue(ch, idx1);

		int diff[] = new int[REPAIR_BUFF_LEN];

		// 差分抽出
		int old_v = 0;
		for (int i = 0; i <= REPAIR_BUFF_LEN; i++) {
			int readIndex = idx0 - REPAIR_BUFF_LEN + i;
			int v = this.byteData.getValue(ch, readIndex);
			if (i != 0) {
				diff[i - 1] = v - old_v;
			}
			old_v = v;
		}

		// 自己相関の算出
		double[] autoCorrelation = new double[REPAIR_BUFF_LEN - AC_WINDOW];
		for (int shift=0 ; shift < REPAIR_BUFF_LEN - AC_WINDOW; shift++) {
			double sum0 = 0;
			double sum1 = 0;
			double sum2 = 0;

			for (int i=0 ; i < AC_WINDOW ; i++) {
				sum0 += 1.0 * diff[i] * diff[i + shift];
				sum1 += square(diff[i]);
				sum2 += square(diff[i+shift]);
			}
			double divisor = Math.sqrt(sum1) * Math.sqrt(sum2);
			if (divisor == 0) {
				// 全て0の場合。
				autoCorrelation[shift] = 0;
				System.out.println( " zero: " + shift);
			}
			else {
				autoCorrelation[shift] = (1.0 / AC_WINDOW) * sum0 / divisor;
			}
		}

		// 最大
		int maxAc = 0;
		double maxAcVal = 0;
		int processPhase = 0;
		double max = 0;
		for (int i=0 ; i < REPAIR_BUFF_LEN - AC_WINDOW ; i++) {
			if (processPhase == 0) {
				if (autoCorrelation[i] < 0) {
					processPhase = 1;
				}
			} else if (processPhase == 1) {
				if (autoCorrelation[i] > 0) {
					processPhase = 2;
					max = autoCorrelation[i];
				}
			} else if (processPhase == 2) {
				if (autoCorrelation[i] > max) {
					max = autoCorrelation[i];
					maxAcVal = autoCorrelation[i];
					maxAc = i;
				}
				else if (autoCorrelation[i] < max) {
					break;
				}
			}
		}

		// 周波数が分からなかった場合
		if (maxAc < 100) {
			maxAc = 130;
		}
		/*
		if (maxAc < 100) {
			repair_remove(ch, idx0, idx1);
			return;
		}
		*/

		/*
		int textureValue0 = this.byteData.getValue(ch, idx0 - (idx1 - idx0) - maxAc);
		int textureValue1 = this.byteData.getValue(ch, idx0 - maxAc);

		for (int i = idx0 + 1 ; i < idx1 ; i++) {
			try {
			int repairValue = (int)
					Math.round(
							 // diff[ diff.length - (idx1 - i)  - maxAc] +

							// diff[ diff.length - (idx1 - idx0) - 2 * maxAc + (i - idx0)] +
							diff[ diff.length - maxAc - (idx1 - idx0) + (i - idx0) ] +
							(double)(value1 - value0  -  ( textureValue1 - textureValue0 )   ) * (i - idx0) / (idx1  - idx0) + value0
							);
			this.byteData.setValue(ch, i, repairValue);
			} catch(Exception e) {
				System.out.println("i: " + i);
			}
		}
		*/

		int offset = 2 * maxAc - (idx1 - idx0);
		int textureValue0 = this.byteData.getValue(ch, idx0 - (idx1 - idx0) - offset);
		int textureValue1 = this.byteData.getValue(ch, idx0 - offset);

		for (int i = idx0 + 1 ; i < idx1 ; i++) {
			// 周りの値と馴染ませる度合い
			double fitRatio = 1;
			final int FIT_WIDTH = 20;
			if (i < idx0 + 1  + FIT_WIDTH) {
				fitRatio = (i - idx0) / (double) FIT_WIDTH;
			}
			else if (i > idx1 + 1  - FIT_WIDTH) {
				fitRatio = (idx1 - i) / (double) FIT_WIDTH;
			}

			// 拾ってきた値を周りの値と馴染ませて書き込む
			try {
				// 拾ってきた値
				double repairValue =
						this.byteData.getValue(ch, i - (idx1 - idx0) - offset)
								-  textureValue0 +

								(double)(value1 - value0  -  ( textureValue1 - textureValue0 )   ) * (i - idx0) / (idx1  - idx0) + value0;
				// 元の値
				double orgValue = this.byteData.getValue(ch, i);
				// 馴染ませた値
				double fitValue = fitRatio * repairValue + (1 - fitRatio) * orgValue;

				int value = (int)Math.round(fitValue);
				this.byteData.setValue(ch, i, value);
			} catch(Exception e) {
				System.out.println("i: " + i);
			}
		}

	}

	void repair2() {
		int ch0cnt = 0;
		int ch1cnt = 0;

		for(FoundNoise n : noiseList) {
			int ch = n.ch;
			/*
			int idx0 = n.start - n.length;
			int idx1 = n.start + 2 * n.length;
			*/
			int idx0 = n.start;
			int idx1 = n.start + n.length;
			// 表示用集計データ
			if (ch == 0) {
				ch0cnt ++;
			}
			if (ch == 1) {
				ch1cnt ++;
			}
			try {
				repair_a_point(ch, idx0, idx1);
			} catch(ArrayIndexOutOfBoundsException e) {
				System.err.println("exception in " + ch + " " + ch0cnt + " " + ch1cnt + " " + n.start);
			}
		}

		System.out.println("ch0: " + ch0cnt + "   ch1: " + ch1cnt);
	}


	void repair2_mute() {
		int ch0cnt = 0;
		int ch1cnt = 0;

		for(FoundNoise n : noiseList) {
			int ch = n.ch;
			/*
			int idx0 = n.start - n.length;
			int idx1 = n.start + 2 * n.length;
			*/
			int idx0 = n.start;
			int idx1 = n.start + n.length;
			// 表示用集計データ
			if (ch == 0) {
				ch0cnt ++;
			}
			if (ch == 1) {
				ch1cnt ++;
			}
			try {
//				repair_remove(ch, idx0, idx1);
				repair_mute(ch, idx0, idx1);
			} catch(ArrayIndexOutOfBoundsException e) {
				throw e;
				// System.err.println("exception  in " + ch + " " + ch0cnt + " " + ch1cnt + " " + n.start);
			}
		}

		System.out.println("ch0: " + ch0cnt + "   ch1: " + ch1cnt);
	}

	void save(String inputFile, String outputFile) throws IOException {
	      DataInputStream inputStream =
		          new DataInputStream(
		              new BufferedInputStream(
		                  new FileInputStream(inputFile)));

	      byte[] header = new byte[startOffset + BYTE_LENGTH * 2];
	      inputStream.read(header);
	      inputStream.close();

	      DataOutputStream outputStream =
	          new DataOutputStream(
	              new BufferedOutputStream(
	                  new FileOutputStream(outputFile)));

	      outputStream.write(header);
	      outputStream.write(data);
	      outputStream.close();
	}

	static void debugOptionCheck(String debugPhase, Main n, String s, String file) {
		if (s.equals("--" + debugPhase)) {
			DebuggingPrompt.debug(n, file);
			System.exit(0);
		}
	}

	ByteData debug_orgByteData;
	void enableOrgByte() {
		debug_orgByteData = byteData.getCopy();
	}

	static public void main(String arg[]) throws Exception {
		if (arg.length < 2 || arg[0].equals(arg[1])) {
			System.out.println("java com.sado.soundUi.NRUtil inputFile outputFile");
			return;
		}

		String inputFile = arg[0];
		String outputFile = arg[1];
		Main n = new Main(inputFile);

		debugOptionCheck("debug0", n, arg[arg.length - 1], inputFile);
		if (arg[arg.length - 1].startsWith("--debug")) {
			n.enableOrgByte();
		}

		// 検出方法1 （比較的大きいものを探す）
		n.check(100, 5000, 0.0015, 50);
		n.repair2();
		debugOptionCheck("debug", n, arg[arg.length - 1], inputFile);

/*
		// 検出方法2 （比較的小さいものを探す）
		n.check(5, 200, 0.001, 10);
		n.repair2_mute();
		debugOptionCheck("debug", n, arg[arg.length - 1], inputFile);
*/

		/*
		n.check(4, 8, 0.003);
		n.repair();

		n.check(5, 20, 0.0015);
		n.repair2();
		n.check(5, 20, 0.0010);
		n.repair2();

		n.check(3, 15, 0.0005);
		n.repair2();
		*/

		/*
		n.check(10, 20, 0.0001);
		n.repair2();
		*/

		n.save(inputFile, outputFile);
	}
}
