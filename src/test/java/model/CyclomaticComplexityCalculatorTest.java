package model;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CyclomaticComplexityCalculatorTest {
    private static final int EXPECTED_OUTPUT = 24;
    private MethodDeclaration testMethodParsed;

    void testMethod(int x, int y) {//5+7+9+6-3=24
        if (x <= 10) {
            return;
        }
        switch (x) {
            case 1: {
                return;
            }
            case 15: {
                x++;
            }
            default:
        }

        for (int i = 0; i < x; i++) {

        }
        if (x == 1) {
            return;
        } else if (x == 2) {
            return;
        } else if (x == 3) {
            if (y == 0) {

            } else if (y == 1) {

            } else {
                return;
            }

        } else {
            for (int i = 0; i < 5; i++) {
                x++;
            }
        }
        for (int i = 0; i < 20; i++) {
            for (int j = 0; j < 20; j++) {
                if (x == 5) {
                    return;
                }
            }
        }
        if (x == 1) {
            return;
        }
        if (x == 4) {
            return;
        }
        if (x == 20) {
            for (int i = 0; i < 10; i++) {
                x++;
            }
            return;
        } else {
            for (int i = 0; i < 10; i++) {
                x++;
            }
            if (x > 5) {
                if (x > 20) {
                    if (x > 100) {
                        return;
                    } else {
                        return;
                    }
                } else {
                    return;
                }
            } else if (2 * x < 1000) {
                return;
            }
            do {
                x++;
            } while (x < 500);
            if (x != 2000) {
                return;
            } else {
                return;
            }
        }
    }

    @BeforeAll
    void parseTestMethod() throws FileNotFoundException {
        JavaParser parser = new JavaParser();
        String pathToThisFile = System.getProperty("user.dir").toString() + "/src/test/java/model/CyclomaticComplexityCalculatorTest.java";
        File thisClassFile = new File(pathToThisFile);

        Optional<CompilationUnit> parsedFile = parser.parse(thisClassFile).getResult();

        CompilationUnit parsedClass = parsedFile.get();
        List<MethodDeclaration> listOfMethods = parsedClass.findAll(MethodDeclaration.class,
                method -> method.getNameAsString().equals("testMethod"));

        this.testMethodParsed = listOfMethods.get(0);
    }

    @Test
    void cc_parsingTestMethod_shouldReturn24(){
        CyclomaticComplexityCalculator ccCalc = new CyclomaticComplexityCalculator();
        int result = ccCalc.computeComplexityForMethod(this.testMethodParsed);
        Assertions.assertEquals(result, EXPECTED_OUTPUT);
    }
}