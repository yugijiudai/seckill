//存放主要交互逻辑js代码
const seckill = {
    //封装秒杀相关ajax的url
    url: {
        now: function () {
            return ctx + '/seckill/time/now';
        },
        exposer: function (seckillId) {
            return ctx + '/seckill/' + seckillId + '/exposer';
        },
        execution: function (seckillId, md5) {
            return ctx + '/seckill/' + seckillId + '/' + md5 + '/execution';
        }
    },
    /**
     * 验证手机号
     * @param phone 手机号
     */
    validatePhone: function (phone) {
        return phone && phone.length == 11 && !isNaN(phone);
    },
    /**
     * 处理秒杀
     */
    handleSeckill: function (seckillId, node) {
        // 获取秒杀地址,控制显示逻辑,执行秒杀
        node.hide().html('<button class="btn btn-primary btn-lg" id="killBtn">开始秒杀</button>');
        $.get(seckill.url.exposer(seckillId), {}, function (result) {
            console.debug(result);
            // 在回调函数中,执行交互流程
            if (result && result['success']) {
                let exposer = result['data'];
                if (!exposer['exposed']) {
                    // 未开启秒杀,重新计算计时逻辑
                    seckill.countdown(seckillId, exposer['now'], exposer['start'], exposer['end']);
                }
                else {
                    let killUrl = seckill.url.execution(seckillId, exposer['md5']);
                    console.debug(`秒杀地址:${killUrl}`);
                    // 绑定一次点击事件
                    $('#killBtn').one('click', function () {
                        //执行秒杀请求
                        // 1:先禁用按钮
                        $(this).addClass('disabled')
                        // 2:发送秒杀请求执行秒杀
                        $.post(killUrl, {}, function (result) {
                            if (result && result['success']) {
                                let killResult = result['data'];
                                let state = killResult['state'];
                                let stateInfo = killResult['stateInfo'];
                                // 显示秒杀结果
                                node.html('<span class="label label-success">' + stateInfo + '</span>');
                            }
                        });
                    });
                    node.show();
                }
            }
        });
    },
    countdown: function (seckillId, nowTime, startTime, endTime) {
        let seckillBox = $('#seckill-box');
        // 时间判断
        if (nowTime > endTime) {
            seckillBox.html('秒杀结束!');
        }
        else if (nowTime < startTime) {
            // 秒杀未开始,计时事件绑定,加一秒防止客户的时间偏移
            seckillBox.countdown(new Date(startTime + 1000), function (event) {
                // 时间格式
                seckillBox.html(event.strftime('秒杀倒计时: %D天 %H时 %M分 %S秒'));
                // 时间完成后回调事件
            }).on('finish.countdown', function () {
                seckill.handleSeckill(seckillId, seckillBox);
            });
        }
        else {
            seckill.handleSeckill(seckillId, seckillBox);
        }
    },
    //详情页秒杀逻辑
    detail: {
        // 详情页初始化
        init: function (params) {
            // 手机验证和登录,计时交互
            // 在cookie中查找手机号
            let killPhone = $.cookie('killPhone');
            // 验证手机号
            if (!seckill.validatePhone(killPhone)) {
                // 绑定phone
                //控制输出
                let killPhoneModal = $('#killPhoneModal');
                killPhoneModal.modal({
                    //显示弹出层
                    show: true,
                    //禁止位置关闭
                    backdrop: 'static',
                    // 关闭键盘事件
                    keyboard: false
                });
                $('#killPhoneBtn').click(function () {
                    let inputPhone = $('#killPhoneKey').val();
                    console.debug(`手机号:${inputPhone}`);
                    if (seckill.validatePhone(inputPhone)) {
                        // 电话写入cookie
                        $.cookie('killPhone', inputPhone, {expires: 7, path: '/seckill'});
                        // 刷新页面
                        window.location.reload();
                    }
                    else {
                        $('#killPhoneMessage').hide().html('<label class="label label-danger">手机号错误!</label>').show(300);
                    }
                });
            }
            //已经登录,开始计时交互
            $.get(seckill.url.now(), {}, function (result) {
                console.debug(result);
                if (result && result['success']) {
                    let nowTime = result['data'];
                    //时间判断,计时交互
                    seckill.countdown(params['seckillId'], nowTime, params['startTime'], params['endTime']);
                }
            });
        }
    }
};
