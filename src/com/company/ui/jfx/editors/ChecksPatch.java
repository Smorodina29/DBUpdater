package com.company.ui.jfx.editors;

import com.company.check.Check;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Александр on 11.04.2017.
 */
public class ChecksPatch {

    List<Check> updated = new ArrayList<>();
    List<Check> deleted = new ArrayList<>();
    List<Check> created = new ArrayList<>();;

    public void addUpdated(Check edited) {
        //ensure that check will be the one (with such id) in the patch
        updated = removeAllWith(edited.getId(), updated);
        deleted = removeAllWith(edited.getId(), deleted);
        updated.add(edited);
    }


    public void addDeleted(Check deletedCheck) {
        //ensure that check will be the one (with such id) in the patch
        updated = removeAllWith(deletedCheck.getId(), updated);
        deleted = removeAllWith(deletedCheck.getId(), deleted);

        deleted.add(deletedCheck);
    }

    private ArrayList<Check> removeAllWith(String id, List<Check> updated) {

        ArrayList<Check> newList = new ArrayList<>();

        for (Check check : updated) {
            if (!id.equals(check.getId())) {
                newList.add(check);
            }
        }
        return newList;
    }


    public boolean isEmpty() {
        return updated.isEmpty() && deleted.isEmpty() && created.isEmpty();
    }

    public boolean isNotEmpty() {
        return !isEmpty();
    }

    public void clear() {
        updated.clear();
        deleted.clear();
        created.clear();
    }


    public List<Check> getUpdated() {
        return updated;
    }

    public List<Check> getDeleted() {
        return deleted;
    }

    @Override
    public String toString() {
        return "ChecksPatch{" +
                "updated=" + updated +
                ", deleted=" + deleted +
                ", created=" + created +
                '}';
    }

    public void updateCreated(Check edited, Check oldValue) {
        created.remove(oldValue);
        created.add(edited);
    }

    public List<Check> getCreated() {
        return created;
    }
}
