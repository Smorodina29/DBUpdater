package com.company.check;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Александр on 18.03.2017.
 */
public class ChecksHolder {
    private static ChecksHolder ourInstance = new ChecksHolder();
    private HashMap<String, List<Check>> keyToChecksMap;

    public static ChecksHolder getInstance() {
        return ourInstance;
    }

    private ChecksHolder() {
        keyToChecksMap = new HashMap<>();

        //address
        keyToChecksMap.put(generateKey("address", "address"), createChecksForAddressAddress());
        keyToChecksMap.put(generateKey("address"), createChecksForAddingInAddress());

        //dbusers
        keyToChecksMap.put(generateKey("dbusers", "firstname"), createChecksForDbUsers());
        keyToChecksMap.put(generateKey("dbusers", "middlename"), createChecksForDbUsers());
        keyToChecksMap.put(generateKey("dbusers", "lastname"), createChecksForDbUsers());
        keyToChecksMap.put(generateKey("dbusers", "contactPhone"), createChecksForDbUsers());
        keyToChecksMap.put(generateKey("dbusers", "contactMail"), createChecksForDbUsers());
        keyToChecksMap.put(generateKey("dbusers", "deleted"), createChecksForDbUsers());
    }

    private List<Check> createChecksForAddingInAddress() {
        return new ArrayList<>();
    }

    private List<Check> createChecksForAddressAddress() {
        ArrayList<Check> list = new ArrayList<>();
        list.add(new UniqueIdCheck());
        list.add(new UniqueRowsCheck());
        list.add(new PresentRowsCheck());
        return list;
    }

    private List<Check> createChecksForDbUsers() {
        ArrayList<Check> list = new ArrayList<>();
        list.add(new UniqueIdCheck());
        list.add(new UniqueRowsCheck());
        return list;
    }

    /**
     * Поиск всех проверок для колонки
     * @param tableName Имя таблицы
     * @param columnName Имя колонки
     * @return Список проверок или null
    * */
    public List<Check> getChecksFor(String tableName, String columnName) {
        String key = generateKey(tableName, columnName);
        return keyToChecksMap.get(key);
    }

    /**
     * Поиск всех проверок для таблицы
     * @param tableName Имя таблицы
     * @return Список проверок или null
     * */
    public List<Check> getChecksFor(String tableName) {
        return keyToChecksMap.get(generateKey(tableName));
    }

    private String generateKey(String... parts) {
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            sb.append(part);
        }
        return sb.toString();
    }

}
