チームm_cre (自然言語処理部門)
====

[人狼知能プレ大会@GAT2017](http://aiwolf.org/event-2/gat2017) 自然言語処理部門の出場エージェントです。

## 必要ライブラリ等

### OSにインストールするもの
* juman
  - ```dic/makedic.sh```を参考に辞書をコンパイルしてください
  - [このへん](http://d.hatena.ne.jp/knaka20blue/20110320/1300627864)を参考に```~/.jumanrc```に```dic```(フルパスで)を設定してください。
  - インストール先が ```/usr/local/bin/juman``` ではない場合はClauseクラスから呼んでいるKNPのコンストラクタの記載を変更する必要があります。

* knp
  - インストール先が ```/usr/local/bin/knp``` ではない場合はClauseクラスから呼んでいるKNPのコンストラクタの記載を変更する必要があります。

### ライブラリ
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
* McrePlayer.jar
  + [mcre/aiwolf-gat2017](https://github.com/mcre/aiwolf-gat2017)をjarに固めたもの
  + 0.4.4対応エージェントであればどんなプレイヤーでもなんらかの動作はするようにはなっているはずです。(その場合 ```McreNlpPlayer```クラスで指定しているプレイヤーを変更してください)

## 連絡先
* [@m_cre](https://twitter.com/m_cre)

## License
* MIT
  + see LICENSE