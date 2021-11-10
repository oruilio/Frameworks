package batis;

import entity.Money;

import java.util.List;

public interface MoneyMapper {

    public Money findById(Integer id);

    public List<Money> findAll();
}
