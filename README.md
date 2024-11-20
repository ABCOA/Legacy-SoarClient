# Legacy SoarClient
此项目是由EldoDebug开发的主要面向现代和强大的PvP客户端！  
此Fork是由我(ABCOA)接续开发的版本。我仅为此项目爱好者，与EldoDebug无关。
## ABCOA的开发计划
### 短期计划：
- [ ] 修复已知的bug （我水平不足，刚接触开发）
### 长期计划：
- [ ] 深度优化代码
- [ ] 新增新功能
## SoarClient的官方开发是否已经停止？
部分是的，v8.0正在开发中，尽管进度很慢
## 如何启动（作为普通用户，不参与开发）
从发布页面下载SoarClient.zip并运行start.bat（离线版）
## 如何设置项目（作为开发者）
EldoDebug使用eclipse作为主要IDE，所以他只知道eclipse的设置方法  
但是，与Forge的设置方法完全相同   
我使用IntelliJ IDEA作为主要IDE
### Eclipse
```
gradlew setupdecompworkspace
```
```
gradlew eclipse
```
### IntelliJ IDEA
idea提供了gradle插件，可以直接导入gradle项目，并且会自动下载gradle依赖  
你所需要做的就是看到左下角长得像锤子一样的图标，点击构建项目即可  
我在build.gradle中添加了阿里云镜像，所以下载速度会很快，便于中国开发者开发  
如果你不知道如何使用gradle插件，可以参考[这个教程](https://www.jetbrains.com/help/idea/gradle.html)









# Legacy SoarClient
The project is mainly aimed at a modern and powerful PvP client developed by EldoDebug!
This Fork is a version developed by me (ABCOA). I am only a hobbyist for this project and have nothing to do with EldoDebug.
## ABCOA's development plan
### Short-term plan:
- [ ] Fix known bugs (I have insufficient level and just started development)
### Long-term plan:
- [ ] Deeply optimize the code
- [ ] Add new features
## Has the official development of SoarClient stopped?
Partially Yes, v8.0 is being developed, albeit very slowly
## How to launch (as an ordinary user, not involved in development)
Download SoarClient.zip from the release page and run start.bat (offline version)
## How to set up the project (as a developer)
EldoDebug uses eclipse as the main IDE, so he only knows the eclipse setting method  
But the setting method is exactly the same as Forge  
I use IntelliJ IDEA as the main IDE
### Eclipse
```
gradlew setupdecompworkspace
```
```
gradlew eclipse
```
### IntelliJ IDEA
idea provides a gradle plugin, which can directly import gradle projects and will automatically download gradle dependencies  
All you need to do is to see the icon that looks like a hammer in the lower left corner, click to build the project  
I added the Aliyun mirror in build.gradle, so the download speed will be very fast, which is convenient for Chinese developers to develop
If you don't know how to use the gradle plugin, you can refer to [this tutorial](https://www.jetbrains.com/help/idea/gradle.html)
