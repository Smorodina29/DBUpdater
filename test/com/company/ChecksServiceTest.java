package com.company;

import com.company.check.*;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by Александр on 18.03.2017.
 */
public class ChecksServiceTest {
    @Test
    public void checksGetForUpdateAddressAddress() {
        ArrayList<Check> expected = new ArrayList<>();
        expected.add(new AllValidationCheck("1", "select count(distinct id) from %tempTable%", "Проверка на уникальность ID во временной таблице.", "Таблица содержит дубликаты индентификаторов.", CheckType.ERROR));
        expected.add(new ZeroValidationCheck("2", "select count(*) from %tempTable% u join %targetTable% a on a.id = u.id", "Проверка на наличие обновляемых записей в БД.", "Некоторые идентификаторы ID не найдены.", CheckType.WARNING));
        expected.add(new AllValidationCheck("3", "select count(*) from %tempTable% u join %targetTable% a on a.id = u.id where where a.%columnName% = u.%columnName%", "Проверка на уникальность записей.", "В таблице присутствуют дубликаты.", CheckType.ERROR));

        List<Check> actual = ChecksService.getChecksForUpdate("users", "lastname");
        assertEquals(expected, actual);
    }

    @Test
    public void checksGetForAddAddress() {
        ArrayList<Check> expected = new ArrayList<>();
        List<Check> actual = ChecksService.getChecksForAdd("users");
        assertEquals(expected, actual);
    }
}
