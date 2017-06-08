-- 数据库初始化脚本 http://www.imooc.com/learn/631

# 创建数据库
-- CREATE DATABASE seckill;
# 使用数据库
-- USE seckill;
DROP TABLE IF EXISTS seckill;
-- 创建秒杀数据库表
CREATE TABLE seckill (
  seckill_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '商品库存id',
  name VARCHAR(120) NOT NULL COMMENT '商品名称',
  number INT NOT NULL COMMENT '库存数量',
  create_time TIMESTAMP NOT NULL DEFAULT current_timestamp COMMENT '创建时间',
  start_time TIMESTAMP NOT NULL COMMENT '秒杀开启时间',
  end_time TIMESTAMP NOT NULL COMMENT '秒杀结束时间',
#   create_time TIMESTAMP NOT NULL COMMENT '创建时间',
  PRIMARY KEY (seckill_id),
  KEY idx_start_time(start_time),
  KEY idx_end_time(end_time),
  KEY idx_create_time(create_time)
)
  ENGINE = InnoDB
  AUTO_INCREMENT = 1000
  DEFAULT CHARSET = utf8
  COMMENT = '秒杀库存表';

# mysql5.5中TIMESTAMP NOT NULL DEFAULT current_timestamp会报错，所以这里用触发器来替代
# DROP TRIGGER IF EXISTS `update_seckill_trigger`;
# CREATE TRIGGER `update_seckill_trigger` BEFORE INSERT ON seckill
# FOR EACH ROW SET NEW.create_time = current_timestamp();


-- 初始化数据
INSERT INTO
  seckill(name, number, start_time, end_time)
VALUES
  ('1000元秒杀iphone6', 100, '2016-11-01 00:00:00', '2017-11-02 00:00:00'),
  ('500元秒杀ipad2', 200, '2016-11-01 00:00:00', '2017-11-02 00:00:00'),
  ('300元秒杀小米4', 300, '2016-11-01 00:00:00', '2017-11-02 00:00:00'),
  ('200元秒杀红米note', 400, '2016-11-01 00:00:00', '2017-11-02 00:00:00');

-- 秒杀成功明细表
-- 用户登录认证相关信息
DROP TABLE IF EXISTS success_killed;
CREATE TABLE success_killed(
  seckill_id BIGINT NOT NULL COMMENT '秒杀商品id',
  user_phone BIGINT NOT NULL COMMENT '用户手机号',
  state TINYINT NOT NULL DEFAULT -1 COMMENT '状态标识:-1:无效 0:成功 1:已付款 2:已发货',
  create_time TIMESTAMP NOT NULL DEFAULT current_timestamp COMMENT '创建时间',
  PRIMARY KEY (seckill_id, user_phone),/*联合主键*/
  KEY idx_create_time(create_time)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8
  COMMENT = '秒杀成功明细表';

# show create TABLE seckill;
# ALTER TABLE seckill DROP  INDEX idx_create_time,ADD INDEX idx_c_s(start_time,create_time)
