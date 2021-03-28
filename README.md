# アナログレコードのスクラッチノイズを軽減する試作

## abstract / 概要

アナログレコードから録音された音声データを処理して、スクラッチノイズを軽減します。
プチプチという雑音を消し去るわけではなく、バチバチというつらいレベルのを消します。

## Motivation / 動機(archive.org の Unlocked Recordings)

米国の著作権法改正（MMA）を受け、1972年までのアナログ音源がインターネット・アーカイブに急増してることが紹介されました。
(SP/LP/EPのディスクから取り込まれたデジタルデータが公開されています)

https://archive.org/details/unlockedrecordings?sort=-addeddate

https://twitter.com/hisamichi/status/1365149629990400003

非常に素晴らしい演奏が公開されていますが、ただ、ときどき眉間に皺が寄るレベルのスクラッチノイズが記録されていたりします。
「あまりにすごいのは、ちょっと、取り除けないか」という試作を行いました。

## How To Use / 使用方法

### 事前に行うこと

事前に flac を wav に変換します。

```
ffmpeg -i inputfile.flac  -vn -ac 2 -ar 96000 -acodec pcm_s24le -f wav  inputfile.wav
```

### 変換

以下のコマンドラインで変換します。このプログラムは、波形を全部メモリに読み込みます。

```
java -Xmx8g  com.github.novisoftware.noiseRemove.Main  inputfile.wav outputfile.wav
```

第3引数に「--debug」を指定すると、デバッグ用のウィンドウが開きます。

### 波形のチェック

一度試してはあるけれど、必ず波形のチェックを行ってから再生してください。
耳とか、音響機器が破損するリスクがあります。

無料の音声ファイルを編集するソフトとして、たとえば「Sazanami」があります。
http://hp.vector.co.jp/authors/VA027069/

（元の波形と同じようなものが表示されるかを確認します。
似ても似つかないものが表示されたら、再生しないでください。）

### 波形のチェック

さらに以下のようなコマンドラインで、再度flacに戻るかもしれません（詳しく調べていません）。
```
ffmpeg -i outputfile.wav -vn  outputfile.flac
```

## 処理方法の大づかみな説明

波形ファイルで尖った場所を見つけて、そこを直線で塗りつぶしています(図1)。
見つける場所はある程度真面目行っていますが、塗りつぶしは「やっつけ仕事」です。
自己相関係数等を使用して、少し前の波形で合うところを探して貼り付ける方が、より良さそうとは思います。

![図1: 処理の概要](/doc/img/fig1.png) 

追記.

少し前の似たような波形を探してきてそれをなぞるような処理にしてみました。

![図2: 修復例](/doc/img/fig1.png) 

![図3: 修復例](/doc/img/fig2.png) 

## その他

動作の確認は、スヴャトスラフ・リヒテルの「前奏曲とフーガOp.87（ショスタコーヴィチ）」（1970年）でのみ、行っています。
（他の演奏を処理すると、全然だめかもしれません。試していこうと思っています）
https://archive.org/details/lp_prludes-et-fugues-op-n-4-12-14-15-1-et-23_dmitri-shostakovich-sviatoslav-richter


