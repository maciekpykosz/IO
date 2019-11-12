package model;

import com.github.javaparser.ParseResult;
import com.github.javaparser.Range;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.utils.SourceRoot;

import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class DependencyFinder {
    public List<DependencyObj> getFilesDependencies(String absoluteDirectoryPath){//Function which construct files dependency for STORY 1

        List<DependencyObj> filesDependencies = new LinkedList<>();
        List<TypeDeclaration> classList = new LinkedList<>();
        SourceRoot root = new SourceRoot(Paths.get(absoluteDirectoryPath));
        List<ParseResult<CompilationUnit>> resultList = root.tryToParseParallelized();

        List<Optional<CompilationUnit>> compilationList =
                resultList.stream().map(ParseResult::getResult).collect(Collectors.toList());
        for(Optional<CompilationUnit> result : compilationList) {
            if(!result.isPresent()) continue; //skip empty object
            for(TypeDeclaration typeDec : result.get().getTypes()) {
                DependencyObj obj = new DependencyObj(typeDec.getNameAsString());

                //Size of file represented as lines count
                Optional<Range> fileRange = typeDec.getRange();
                if(fileRange.isPresent()){
                    int lineCount = fileRange.get().end.line;
                    obj.setWeight(lineCount);
                } else {
                    //range not available - set default value
                    obj.setWeight(0);
                }

                filesDependencies.add(obj);
                classList.add(typeDec);
            }
        }

        for (DependencyObj filesDependency : filesDependencies) {
            int a = 1; //temp
            for (TypeDeclaration classItem : classList) {
                /*Optional<SimpleName> dependency = Navigator.findSimpleName(classItem, filesDependency.getName());
                if (dependency.isPresent() && !classItem.getNameAsString().equals(filesDependency.getName())){
                    DependencyObj temp = new DependencyObj(classItem.getNameAsString());
                    filesDependency.addDependency(temp);
                }*/

                int dependenciesNum = classItem.findAll(SimpleName.class, (v) -> {
                    return ( v.asString().equals(filesDependency.getName())
                            && !classItem.getNameAsString().equals(filesDependency.getName()) );
                }).size();

                if(dependenciesNum > 0) {
                    int dependencyIndex = filesDependencies.indexOf(new DependencyObj(classItem.getNameAsString()));
                    DependencyObj temp = filesDependencies.get(dependencyIndex);

                    if(filesDependency.getDependencyList().computeIfPresent(temp, (k, v) -> v + dependenciesNum) == null) {
                        filesDependency.getDependencyList().put(temp, dependenciesNum);
                    }

                }
            }
        }
        return filesDependencies;
    }
}
