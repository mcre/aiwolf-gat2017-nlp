チームm_cre (自然言語処理部門)
====

[人狼知能プレ大会@GAT2017](http://aiwolf.org/event-2/gat2017) 自然言語処理部門の出場エージェントです。

## プログラム解説

* [pdf](/doc/20170309_slide.pdf)

## 動作環境

* macOS (Sierra)
  + Windowsでもjuman, knp を呼び出す部分をうまく変えれば動くと思います。
* jdk (8)

### OSにインストールするもの

* juman (7.01)
  + ```dic/makedic.sh```を参考に辞書をコンパイルしてください
  + [このへん](http://d.hatena.ne.jp/knaka20blue/20110320/1300627864)を参考に```~/.jumanrc```に```dic```(フルパスで)を設定してください。
  + インストール先が```/usr/local/bin/juman```ではない場合はClauseクラスから呼んでいるKNPのコンストラクタの記載を変更する必要があります。

* knp (4.16)
  + インストール先が```/usr/local/bin/knp```ではない場合はClauseクラスから呼んでいるKNPのコンストラクタの記載を変更する必要があります。

### 必要ライブラリ等

* aiwolf-client.jar (0.4.4)
* aiwolf-common.jar (0.4.4)
* aiwolf-server.jar (0.4.4)
* aiwolf-viewer.jar (0.4.4)
* jsonic-1.3.10.jar
* jackson-core-2.8.1.jar
* jackson-annotations-2.8.0.jar
* jackson-databind-2.8.5.jar
* java-juman-knp.jar
  + [mychaelstyle/java-juman-knp](https://github.com/mychaelstyle/java-juman-knp) を(勝手に)jarに固めたもの
* [McrePlayer.jar](https://github.com/mcre/aiwolf-gat2017/blob/master/McrePlayer.jar)
  + 0.4.4対応エージェントであればどんなプレイヤーでもなんらかの動作はするようにはなっているはずです。(その場合```McreNlpPlayer```クラスで指定しているプレイヤーを変更してください)

## クラス説明

* プレイヤー本体
  * net.mchs_u.mc.aiwolf.nlp.agito.McreNlpPlayer

* mainメソッド
  + net.mchs_u.mc.aiwolf.nlp.starter.Starter
    - サーバとクライアント5体が一発で起動します
  + net.mchs_u.mc.aiwolf.nlp.agito.Clause
    - 文章を解析してClauseクラスに格納された結果を確認できます
  + net.mchs_u.mc.aiwolf.nlp.util.KNPChecker
    - Clauseクラスに格納されるまえの詳細な解析結果を確認できます
  + net.mchs_u.mc.aiwolf.nlp.util.LogConverter
    - サーバから吐き出されるlogファイルを読みやすく変換できます
  + net.mchs_u.mc.aiwolf.nlp.util.TransratedMapChecker
    - 自然言語をプロトコルに変換した履歴を確認できます

## 連絡先

* [twitter: @m_cre](https://twitter.com/m_cre)
* [blog](http://www.mchs-u.net/mc/)

## License

* MIT
  + see LICENSE