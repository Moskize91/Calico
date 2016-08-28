Calico
======
![master branch](https://travis-ci.org/Moskize91/Calico.svg?branch=master)

Calico 可以根据预设的规则，通过一个模板和一个资料库，生成一个静态 Web 站点所需的 Html 文件。Calico 会假装自己是一个动态服务器。对 Calico 的规则和模板都是通过假装 Calico 是动态服务器来确立的。

# Calico 的工作原理

具体来说，当 Calico 收到一个 Request，它会解析 Request 的 URL，并通过路由规则匹配到一个模板 View。并通过模板 View 构造 html 页面。模板 View 中可以嵌套用 JavaScript 写成的小脚本，这些小脚本会通过 Request 的参数（从 URL 解析得来）来获取对应的资源，这些资源可以是其他 html 页面，也可以是 markdown 文件，甚至是 json 文件。读取的资源会通过小脚本嵌套再模板 View 最终渲染出的 html 页面中。最终，这个 html 页面作为一个 Response 被返回。

但 Calico 终究不是一个动态 Web 服务器。实际上，Calico 仅仅只能一次性生成一个 Web 站点所需的所有 html 文件。它是这样工作的。

首先，Calico 会挑选一组种子地址，默认是 “``/``” 这个根地址。之后，请求这些种子地址，并渲染出 html 页面。之后在 html 页面中寻找所有指向站内的 URL，再以这些 URL 发起请求渲染更多的 html 页面。这个过程会递归地进行下去，直到找不到更多的指向站内的 URL 地址为止。

这一过程的原理类似爬虫。这样便可保证 Calico 生成的那一堆 html 文件，里面的任何一个指向站内的 URL 都是可以打开的。之后，我们只需要把 Calico 生成的所有文件发布到某个静态 Web 站点即可。

# 安装

Calico 目前只支持 Unix/Linux/macOS 平台。
请确保安装了 git、ant、JDK1.8。

首先，你需要挑选一个 Calico 所安装的目标文件夹，用 Terminal 切换到该文件夹下。然后运行下面这两行命令的任意一行，来 clone 整个项目。

- ``git clone git@github.com:Moskize91/Calico.git``
- ``git clone https://github.com/Moskize91/Calico.git``

之后，输入 ``cd Calico``。

进入项目文件夹之后，请输入 ``./build`` 来构造整个项目。当你看到……

```
BUILD SUCCESSFUL
Total time: 2 seconds
```

的字样，说明 Calico 构造成功。否则，请确认你已经安装了 ant 和 JDK1.8。

之后，输入 ``./install`` 安装 Calico。

其中可能需要你输入 sudo 密码，输入即可。看到

```
Success.
```
的字样，说明 Calico 安装成功。

# 第一个 Calico 模板

对于 Calico 而言，模板＋资源＝站点。
我们先写一个最简单的的，不需要任何资源就可以使用的模板吧。

```
mkdir example
cd example
```

新建一个名为 example 的空文件夹，作为我们的模板文件夹。然后……

```
mkdir view
```

这个名为 view 的文件夹是专门存放模板 View 的。之后……

```
vim ./view/main.html
```

然后按下 ``i`` 输入……

```
<html>
     <body>hello world.</body>
 </html>
```
然后按下 ``ESC`` 再输入 ``:wq`` 并回车保存退出。
确保此时你处于 ``example`` 这个文件夹之中。

最后，输入如下命令以 service 模式启动 Calico……

```
calico service
```

当你看到

```
Running! Point your browser to http://127.0.0.1:8080/ 
```

表明 Calico 启动成功，此时，使用浏览器访问 [127.0.0.1:8080](http://127.0.0.1:8080/)，就可以看到我们的 hello world 页面了。

> ##service 模式
>
> Calico 以这种模式运行时，会在本地监听特定的端口（默认 8080 ）。使用浏览器通过这个端口，可以直接与 Calico 交互。此时 Calico 就像一个动态 Web 服务器一样，针对浏览器的每一次 Request 仅仅生成一个 Response。
>
> 在本地以这种模式执行 Calico，有助于调试你的模板。你对模板中的 View 文件，或 JavaScript 脚本文件，或资源文件进行修改之后，只需要刷新一下浏览器，便可立即看到修改之后的效果。

按下 ``Ctrl + C `` 结束 Calico 的 service 模式。

之后，我们尝试使用 build 模式执行 Calico。

首先确保你处于 ``example`` 文件夹中，输入 ``ls``，此时你应该看到……

```
view
```

仅有一个 view 文件夹，这就是我们刚才新建的。然后，输入如下命令并执行。

```
calico build
```
看到……

```
Clean target directory: /Users/taozeyu/test/example/target
	 delete file /Users/taozeyu/test/example/target

Copy resource files

Generate html pages.
	generate path /
```
表明执行成功了，此时输入 ``ls``，可以看到……

```
target view
```
多出一个 target 文件夹，这是 Calico 生成的。

我们使用 ``ls target`` 查看这个文件夹里面的文件。可以看到……

```
index.html
```
我们使用浏览器打开这个文件，可以看到浏览器中显示……
> hello world

这正是 Calico 使用我们的模板 View 生成的目标 html 文件。

> ## build 模式
>
> Calico 以这种模式执行时，会在从种子地址开始，递归地生成所有的可能的 html 文件。每当它产生一个 html 文件，它便立即搜集这个 html 中每一个指向站内的 URL，并通过这些 URL 继续生成更多的 html 文件。直到没有更多的 html 文件可以生成为止。

# 使用的库：

 - Jsoup-1.8.1
 - markdown-0.4.1
 - NanoHTTPD-2.1.1
