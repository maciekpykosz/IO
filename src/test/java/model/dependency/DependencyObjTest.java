package model.dependency;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.LinkedList;

import static org.junit.jupiter.api.Assertions.*;

public class DependencyObjTest {
    @Test
    public void constructor_validNameAndInitialValues_nameSpecifiedInConstructorZeroWeightBlankDepList() {
        String testObjName = "Test";
        DependencyObj testObject = new DependencyObj(testObjName);
        assertEquals(testObject.getName(), testObjName);
        assertEquals(testObject.getDependencyList().size(), 0);
        assertEquals(testObject.getWeight(), 0);
    }

    @Test
    public void getName_testName_shouldReturnSameStringLikeInConstructor() {
        String testObjName = "testName";
        DependencyObj testObject = new DependencyObj(testObjName);
        assertEquals(testObject.getName(), testObjName);
    }

    @Test
    public void setName_settingNewName_changeNameInObject() {
        String newName = "testName";
        DependencyObj testObject = new DependencyObj("blank");
        testObject.setName(newName);
        assertEquals(testObject.getName(), newName);
    }

    @Test
    public void setWeight_settingNewWeight_changeWeightInObject() {
        int newWeight = 1;
        DependencyObj testObject = new DependencyObj("blank");
        testObject.setWeight(newWeight);
        assertEquals(testObject.getWeight(), newWeight);
    }

    @Test
    public void setDependencyList_addingNotEmptyMap_getterShouldReturnNotEmptyMap() {
        HashMap<DependencyObj, Integer> testMap = new HashMap<>();
        testMap.put(new DependencyObj("testObj"), 2);
        DependencyObj testObject = new DependencyObj("blank");
        testObject.setDependencyList(testMap);
        assertTrue(testObject.getDependencyList().size() > 0);
    }

    @Test
    public void addDependency_addingDependencyToMap_mapContainsNewDependency() {
        DependencyObj testObject = new DependencyObj("blank");
        DependencyObj objToAdd = new DependencyObj("ToAdd");
        testObject.addDependency(objToAdd);
        assertTrue(testObject.getDependencyList().containsKey(objToAdd));
    }

    @Test
    public void addDependency_addingExistingDependencyToMap_increaseWeightInObjectStoredInMap(){
        DependencyObj testObject = new DependencyObj("blank");
        DependencyObj objToAdd = new DependencyObj("ToAdd");
        testObject.addDependency(objToAdd);
        testObject.addDependency(objToAdd);
        assertEquals(testObject.getDependencyList().get(objToAdd), 2);
    }

    @Test
    public void toString_convertingObjToString_stringWithNameAndWeightSeparatedByNewLine(){
        DependencyObj testObject = new DependencyObj("Test");
        testObject.setWeight(1);
        assertEquals(testObject.toString(), "Test\n1");
    }

    @Test
    public void equals_checkingSameObjects_true() {
        DependencyObj testObject = new DependencyObj("blank");
        assertEquals(testObject, testObject);
    }

    @Test
    public void equals_checkingSeparateObjectsWithSameNames_true() {
        DependencyObj testObject = new DependencyObj("blank");
        DependencyObj testObject2 = new DependencyObj("blank");
        assertEquals(testObject, testObject2);
    }

    @Test
    public void equals_checkingObjectsFromDifferedClasses_false() {
        String testObject = "blank";
        DependencyObj testObject2 = new DependencyObj("blank");
        assertNotEquals(testObject, testObject2);
    }

    @Test
    public void hashCode_compareHashCodesOfObjectsWithSameName_codesShouldBeTheSame() {
        DependencyObj testObject = new DependencyObj("blank");
        DependencyObj testObject2 = new DependencyObj("blank");
        assertEquals(testObject.hashCode(), testObject2.hashCode());
    }

    @Test
    public void getStyle_gettingStyleFromNewObject_emptyString() {
        DependencyObj testObject = new DependencyObj("blank");
        assertEquals(testObject.getStyle(), "");
    }

    @Test
    public void setStyle_settingNewStyle_changeStyleInObject() {
        DependencyObj testObject = new DependencyObj("blank");
        testObject.setStyle("color=blue");
        assertNotEquals(testObject.getStyle(), "");
    }

    @Test
    public void id_cretingNewObject_idShouldIncreaseAndCreatedObjectHasOldValue() {
        int actualId = DependencyObj.getIds();
        DependencyObj testObject = new DependencyObj("blank");
        assertEquals(DependencyObj.getIds(), actualId + 1);
        assertEquals(testObject.getId(), actualId);
    }
}
