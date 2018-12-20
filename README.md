# fiction kotlin
## 小说阅读器
<p>小说内容来自网络。
<p>支持自行管理配置书源:使用CSS选择元素。正则表达式提取内容。也可以在意见反馈中登录GitHub账号，提交建议到issue。
<p>阅读记录。
<p>翻页模式（从任阅移植。略有修改）:仿真、平移、覆盖，上下滚动
  
## 项目说明:

1. 网络请求使用 retrofit2.

2. 数据存储使用 room数据库框架 以及mmkv。

3. 页面渲染 大部分使用databinding 框架绑定数据。

4. 业务逻辑 等等 完全依赖于rxjava。

### 数据更新与通知回调。

app页面更新主要依赖于room数据框架的主动推送能力。

1. 数据流向 我认为始终是单向的。修改数据的地方只是在做数据更改，页面刷新通过room框架推送，不做额外处理，尽可能使代码单纯，逻辑清晰。

# 欢迎意见建议与指导。

---
![](https://raw.githubusercontent.com/lTBeL/novel_extra/master/screen/20181218_100523.gif)

---

![](https://raw.githubusercontent.com/lTBeL/novel_extra/master/screen/20181218_100607.gif)

---

![](https://raw.githubusercontent.com/lTBeL/novel_extra/master/screen/20181218_100641.gif)

