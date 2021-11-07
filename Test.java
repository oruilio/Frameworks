package batis;

import entity.Money;

import java.util.List;

public class Test {
    public static void main(String[] args) {
        //创建handler对象
        MyInvocationHandler myInvocationHandler = new MyInvocationHandler();
        //根据接口生成动态代理对象
        MoneyMapper moneyMapper = (MoneyMapper) myInvocationHandler.getInstance(MoneyMapper.class);
        //调用对象方法
        Money money = moneyMapper.findById(1);
//        List<Money> money = moneyMapper.findAll();
        System.out.println(money);
    }
}
