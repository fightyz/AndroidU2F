Android中的keystore支持bks(BouncyCastle KeyStore)格式的证书，因此需要在电脑上生成attestationcert.bks。  
1. 下载对应jdk版本的BouncyCastle。我的jdk版本是jdk1.8.0_45，因此下载bcprov-jdk15on-154。
2. 将bcprov-jdk15on-154.jar复制到%JRE_HOME%\lib\ext，与%JDK_HOME%\jre\lib\ext下  
3. 修改%JRE_HOME%\lib\security\java.security,与%JDK_HOME%\jre\lib\security\java.security 往最后添加，前面已经有10个了 
security.provider.11=org.bouncycastle.jce.provider.BouncyCastleProvider  
4. 输入命令：keytool -genkeypair -alias mykey -keyalg EC -keysize 256 -sigalg SHA256withECDSA -keypass 123456 -validity 365 -storetype bks -keystore attestationcert.bks -storepass 123456 -provider org.bouncycastle.jce.provider.BouncyCastleProvider

参考：
<http://callistaenterprise.se/blogg/teknik/2011/11/24/creating-self-signed-certificates-for-use-on-android/>  
<http://gdfdfg-tech.iteye.com/blog/2051537>  
<http://stackoverflow.com/questions/22399892/java-7-ecc-keypair-generation-with-keytool>  
