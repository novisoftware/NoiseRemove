# NoiseRemove / LP���R�[�h�̃X�N���b�`�m�C�Y���y�����鎎��

## Motivation / ���@

archive.org �� Unlocked Recordings�B

�č��̒��쌠�@�����iMMA�j���󂯁A1972�N�܂ł�LP�������C���^�[�l�b�g�E�A�[�J�C�u�ɋ}�����Ă邱�Ƃ��Љ��܂����B

https://archive.org/details/unlockedrecordings?sort=-addeddate

https://twitter.com/hisamichi/status/1365149629990400003

�f���炵���̂�����ǁA�Ƃ��ǂ��X�N���b�`�m�C�Y�̑傫���̂��L�^����Ă��āA���Ԃ�ᰂ���郌�x���B
����Ȃɐ_�o���ɋC�ɂ���̂Ȃ猳�X�������낤����ǁB

�u���܂�ɂ������̂́A������ƁA��菜���Ȃ����v�Ƃ�������ł��B

## How To Use / �g�p���@

### ���O�ɍs������

���O�� flac �� wav �ɕϊ����܂��B

```
ffmpeg -i inputfile.flac  -vn -ac 2 -ar 96000 -acodec pcm_s24le -f wav  inputfile.wav
```

### �ϊ�

�ȉ��̃R�}���h���C���ŕϊ����܂��B���̃v���O�����́A�g�`��S���������ɓǂݍ��݂܂��B

```
java -Xmx8g  com.github.novisoftware.noiseRemove.Main  inputfile.wav outputfile.wav
```

��3�����Ɂu--debug�v���w�肷��ƁA�f�o�b�O�p�̃E�B���h�E���J���܂��B

### �g�`�̃`�F�b�N

��x�����Ă͂��邯��ǁA�K���g�`�̃`�F�b�N���s���Ă���Đ����Ă��������B
���Ƃ��A�����@�킪�j�����郊�X�N������܂��B

�����̉����t�@�C����ҏW����\�t�g�Ƃ��āA���Ƃ��΁uSazanami�v������܂��B
http://hp.vector.co.jp/authors/VA027069/

�i���̔g�`�Ɠ����悤�Ȃ��̂��\������邩���m�F���܂��B
���Ă������Ȃ����̂��\�����ꂽ��A�Đ����Ȃ��ł��������B�j

### �g�`�̃`�F�b�N

����Ɉȉ��̂悤�ȃR�}���h���C���ŁA�ēxflac�ɖ߂邩������܂���i�ڂ������ׂĂ��܂���j�B
```
ffmpeg -i outputfile.wav -vn  outputfile.flac
```

## ���̑�

����̊m�F�́A�X�����g�X���t�E���q�e���́u�O�t�Ȃƃt�[�KOp.87�i�V���X�^�R�[���B�`�j�v�i1970�N�j�ł̂݁A�s���Ă��܂��B
�i���̉��t����������ƁA�S�R���߂�������܂���B�����Ă������Ǝv���Ă��܂��j
https://archive.org/details/lp_prludes-et-fugues-op-n-4-12-14-15-1-et-23_dmitri-shostakovich-sviatoslav-richter


