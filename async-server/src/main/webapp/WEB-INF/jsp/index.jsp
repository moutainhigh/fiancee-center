<!DOCTYPE html>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta http-equiv="X-UA-Compatible" content="ie=edge">
    <title>SASS财务模块监控</title>
    <script src="https://code.jquery.com/jquery-1.9.1.min.js"></script>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css" type="text/css">
    <script>
        //全局变量
        let url;    //发送的Url
        let box;    //展示区
        let ti;     //时间展示区
        let strNormal = '', strError = '', strWarning = '';
        let newStr = '';
        let data;   //请求返回的数据
        //时间变量
        let myDate, year, month, date, h, m, s, now;

        $(document).ready(function () {
            //展示背景
            box = document.querySelector('.list-box')
            let header = document.querySelector('.list-header')
            let title = document.querySelector('.list-header-center')
            let bodyWidth = document.body.clientWidth
            let bodyHeight = parseInt(window.innerHeight)
            console.log(bodyHeight)
            header.style.width = bodyWidth + 'px'
            header.style.height = bodyWidth * 0.06 + 'px'
            title.style.fontSize = title.clientWidth * 0.04 + 'px'
            title.style.lineHeight = title.clientHeight + 'px'
            box.style.width = bodyWidth - 36 + 'px'
            box.style.height = bodyHeight - header.clientHeight - 100 + 'PX'

            //定时获取服务端数据
            window.setInterval(getData, 3000);

        });

        //获取数据
        function getData() {
            url = '<%=request.getContextPath()%>';
            box = document.querySelector('.list-box');
            $.ajax({
                url: url + "/monitor/getMonitors",
                type: "get",
                headers: {'Content-Type': 'application/json'},
                success: function (res) {
                    //alert(JSON.stringify(res));
                    if (res.status == 'success') {
                        data = res.data;
                        //显示
                        show(data, box);

                    } else {
                        //alert(res.message);
                    }
                }
            });
        }

        //展示数据
        function show(data, box) {
            strNormal = '', strError = '', strWarning = '';
            newStr = '';
            //遍历map中的数据
            $.each(data, function (key, v) {
                //alert('key:'+key);
                //alert(JSON.stringify(v));
                //绿色 正常的
                strNormal = '<div class="list-box-item-normal"><div class="item-top"><div class="top-left"><div class="left-img"></div>' +
                    v.appName + '</div><div class="top-right">' +
                    key + '</div></div><div class="item-footer"><div class="footer-top">' +
                    v.appCode + '</div><div class="footer-bottom">' +
                    v.ip + '</div></div></div>';

                //红色的
                strError = '<div class="list-box-item-error"><div class="item-top-error"><div class="top-left-error"><div class="left-img-error"></div>' +
                    v.appName + '</div><div class="top-right-error">' +
                    key + '</div></div><div class="item-footer-error"><div class="footer-top-error">' +
                    v.appCode + '</div><div class="footer-bottom-error">' +
                    v.ip + '</div></div></div>';
                //灰色的
                strWarning = '<div class="list-box-item-warning"><div class="item-top-warning"><div class="top-left-warning"><div class="left-img-warning"></div>' +
                    v.appName + '</div><div class="top-right-warning">' +
                    key + '</div></div><div class="item-footer-warning"><div class="footer-top-warning">' +
                    v.appCode + '</div><div class="footer-bottom-warning">' +
                    v.ip + '</div></div></div>';
                // 1 OK 0不正常
                switch (v.isOk) {
                    case 1:
                        newStr = newStr + strNormal
                        break
                    case 0:
                        newStr = newStr + strError
                        break
                    case 2:
                        newStr = newStr + strWarning

                }
                console.log('newStr:' + newStr);
                box.innerHTML = newStr;
            })
        }

        //定时刷新时间
        setInterval(getNow, 1000);

        //获取当前时间
        function getNow() {
            myDate = new Date();

            year = myDate.getFullYear();        //获取当前年
            month = myDate.getMonth() + 1;   //获取当前月
            date = myDate.getDate();            //获取当前日


            h = myDate.getHours();              //获取当前小时数(0-23)
            m = myDate.getMinutes();          //获取当前分钟数(0-59)
            s = myDate.getSeconds();

            now = year + '-' + appendLen(month) + "-" + appendLen(date) + " " + appendLen(h) + ':' + appendLen(m) + ":" + appendLen(s);
            ti = document.querySelector('.right-date');
            ti.innerHTML = now;

        }

        //判断是否在前面加0
        function appendLen(s) {
            return s < 10 ? '0' + s : s;
        }


    </script>
</head>
<body>
<div class="list">
    <div class="list-header">
        <div class="list-header-left">
            <div class="left-item">
                <div class="left-item-img"></div>
                <div class="left-item-font">正常</div>
            </div>
            <div class="center-item">
                <div class="center-item-img"></div>
                <div class="center-item-font">异常</div>
            </div>
            <div class="right-item">
                <div class="right-item-img"></div>
                <div class="right-item-font">服务未刷新</div>
            </div>
        </div>
        <div class="list-header-center">
            SASS财务模块监控
        </div>
        <div class="list-header-right">
            <div class="right-box">
                <div class="right-clock"></div>
                <div class="right-date"></div>
            </div>
        </div>
    </div>
    <div class="list-box">

    </div>
</div>
</body>
</html>