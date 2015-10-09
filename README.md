Calico
======

这个程序将用于生成静态 HTML 页面。目前想用其生成一个博客这种程度的页面。博客中的文章（Resource）可以用 Markdown 写，也可以直接写成 HTML。这些文章将由模板（Template）读出，并生成最终的 HTML 页面。

作者：[MOsky泽](http://taozeyu.com)

# 原理

Calico 会假装自己是一个动态 Web 服务器，虽然它最终只能生成静态页面。如图所示，为 Calico 处理一个请求的流程。

![Calico如何工作](https://cloud.githubusercontent.com/assets/6957148/5595888/e8f8ed3a-92b9-11e4-9963-a6bc39623f8a.jpg)

1.	Routes 会将一个地址映射到一个 Template 文件上。例如，对于 /article/log/2015-01-02-hello-world.html ，如果存在存储路径为 /article/log.html 的 Temlate（模板）文件，则 Routes 会将 2015-01-02-hello-world 作为参数，并将这个请求交由 /article/log.html 处理。

	Routes 规则仅仅由 Template 文件的命名和相对项目的路径来决定。

	特别的，如果对于某个请求，刚好有一个路径和名称与之完全相同的 Template 文件存在，则这个 Template 将处理这个请求。例如，对于 /about.html，刚好存在一个 Template文件，为 /about.html。

2.	Template 中内嵌的用 JavaScript 的小脚本与 HTML 内容将最终生成目标 HTML 文件。这个目标文件所存储的地址将和请求的地址完全相同。例如，/article/log/2015-01-02-hello-world.html 最终生成的文件也会存储在 /article/log/2015-01-02-hello-world.html。

3.	Tempalte 中的 JavaScript 小脚本可以读取 Resource（资源），利用 Routes 传来的参数，取出对应的资源。这些资源对于博客而言就是博客里的每一篇文章，这些文章可以写成 Markdown 格式，也可以写成 HTML。每一个文件对应一个资源。可以用 JavaScript 代码取出，如：
R.page("log/2015-01-02-hello-world"); 

当定义好 Template 文件和 Resource 文件之后，就可以开始生成网页了。

但是，Calico 毕竟不是一个动态 Web 服务器。在生成网站之前，Calico 需要知道根地址对应的 Tempalte 文件是哪一个，这需要在启动的时候告诉它。随后，它将生成根页面。

之后，Calico 会读取页面上所有链接指向的 URL 地址，如果地址指向本站，则它会试图用这些地址向 Routes 发出请求，继续生成新的页面。之后再从生成的新页面读取链接重新发出请求，直到遍历完整个网站，找不到新的页面需要生成为止。

整个过程很像爬虫，但这样能确保生成的所有页面的链接都是可以点开的。

# 使用的库：

 - Jsoup-1.8.1
 - markdown-0.4.1
 - NanoHTTPD-2.1.1
