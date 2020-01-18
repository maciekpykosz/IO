package model.dependency;

import com.github.javaparser.ParseResult;
import com.github.javaparser.Range;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.utils.SourceRoot;
import model.CyclomaticComplexityCalculator;

import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class DependencyFinder {
    private List<DependencyObj> lastCreatedDependencies;

    public List<DependencyObj> getLastCreatedDependencies() {
        return lastCreatedDependencies;
    }

    /**
     * Function which construct files dependency for STORY 1
     * @param absoluteDirectoryPath Path where algorithm finds dependencies
     * @return List of files with dependencies
     */
    public List<DependencyObj> getFilesDependencies(String absoluteDirectoryPath){

        List<DependencyObj> filesDependencies = new LinkedList<>();
        List<TypeDeclaration> classList = new LinkedList<>();
        SourceRoot root = new SourceRoot(Paths.get(absoluteDirectoryPath));
        List<ParseResult<CompilationUnit>> resultList = root.tryToParseParallelized();

        List<Optional<CompilationUnit>> compilationList =
                resultList.stream().map(ParseResult::getResult).collect(Collectors.toList());
        for(Optional<CompilationUnit> result : compilationList) {
            if(!result.isPresent()) continue; //skip empty object
            for(TypeDeclaration typeDec : result.get().getTypes()) {
                DependencyObj obj = new FileDependency(typeDec.getNameAsString());
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
                    int dependencyIndex = filesDependencies.indexOf(new FileDependency(classItem.getNameAsString()));
                    DependencyObj temp = filesDependencies.get(dependencyIndex);

                    if(filesDependency.getDependencyList().computeIfPresent(temp, (k, v) -> v + dependenciesNum) == null) {
                        filesDependency.getDependencyList().put(temp, dependenciesNum);
                    }

                }
            }
        }
        lastCreatedDependencies = filesDependencies;
        return filesDependencies;
    }

    public List<DependencyObj> getMethodsDependencies(String absoluteDirectoryPath) { // STORY 2
        List<DependencyObj> dependenciesList = new LinkedList<>();
        List<MethodDeclaration> methodsList = new LinkedList<>();

        SourceRoot root = new SourceRoot(Paths.get(absoluteDirectoryPath));
        List<ParseResult<CompilationUnit>> resultList = root.tryToParseParallelized();

        List<Optional<CompilationUnit>> compilationList =
                resultList.stream().map(ParseResult::getResult).collect(Collectors.toList());

        for (Optional<CompilationUnit> result : compilationList) {
            if (!result.isPresent()) continue; //skip empty object
            for (TypeDeclaration typeDec : result.get().getTypes()) {
                List<BodyDeclaration> members = typeDec.getMembers();
                for (BodyDeclaration member : members) {
                    if (member.isMethodDeclaration()) {
                        MethodDeclaration method = (MethodDeclaration) member;
                        DependencyObj methodDependency = new MethodDependency(method.getNameAsString());
                        CyclomaticComplexityCalculator cyclomaticComplexity = new CyclomaticComplexityCalculator();
                        int cc = cyclomaticComplexity.computeComplexityForMethod(method);
                        methodDependency.setCyclomaticComplexity(cc);
                        dependenciesList.add(methodDependency);
                        methodsList.add(method);
                    }
                }
            }
        }
        for (DependencyObj dependencyObj : dependenciesList) {
            for (MethodDeclaration method : methodsList) {
                /*Optional<MethodCallExpr> methodCall = Navigator.findMethodCall(method, dependencyObj.getName());
                if (methodCall.isPresent() && !method.getNameAsString().equals(dependencyObj.getName())) {
                    int tempIndex = dependenciesList.indexOf(new DependencyObj(method.getNameAsString()));
                    dependencyObj.addDependency(dependenciesList.get(tempIndex));
                }*/
                int dependenciesNum = method.findAll(MethodCallExpr.class, (v) ->
                        (v.getName().asString().equals(dependencyObj.getName()) &&
                                !method.getNameAsString().equals(dependencyObj.getName()))
                ).size();

                if (dependenciesNum > 0) {
                    int dependencyIndex = dependenciesList.indexOf(new MethodDependency(method.getNameAsString()));
                    DependencyObj temp = dependenciesList.get(dependencyIndex);

                    if (dependencyObj.getDependencyList().computeIfPresent(temp, (k, v) -> v + dependenciesNum) == null) {
                        dependencyObj.getDependencyList().put(temp, dependenciesNum);
                    }

                }
            }
        }
        lastCreatedDependencies = dependenciesList;
        DependencyObj.calculateWeightsForMethods(dependenciesList);
        return dependenciesList;
    }

    public List<DependencyObj> getModuleDependencies(String rootFile) {    //story 3
        List<DependencyObj> moduleDependencies = new LinkedList<>();
        Map<PackageDeclaration, Set<DependencyObj>> packagesMethods = new HashMap<>(); //set to have unique method names

        SourceRoot root = new SourceRoot(Paths.get(rootFile));
        List<ParseResult<CompilationUnit>> compilationList = root.tryToParseParallelized();

        List<CompilationUnit> allRootFiles =
                compilationList.stream().map(ParseResult::getResult)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.toList());

        //map all methods to specified package
        for(CompilationUnit classFile : allRootFiles) {
            //get all MethodDeclaration and map it to DependencyObj

            //without same method name recognition
            if(!classFile.getPackageDeclaration().isPresent()) continue;   //skip classes without specified package
            PackageDeclaration classPackage = classFile.getPackageDeclaration().get();
            String packageName = classPackage.getNameAsString();


            List<DependencyObj> allClassMethods = classFile
                    .findAll(MethodDeclaration.class)
                    .stream()
                    .map((method) -> {
                        DependencyObj temp = new ModuleDependency(method.getNameAsString() + "\n" + packageName);
                        //calculating cc
                        CyclomaticComplexityCalculator cyclomaticComplexity = new CyclomaticComplexityCalculator();
                        int cc = cyclomaticComplexity.computeComplexityForMethod(method);
                        temp.setCyclomaticComplexity(cc);
                        return temp;
                    })
                    .collect(Collectors.toList());


            //adding package and methods to packagesMethods
            if(packagesMethods.computeIfPresent(classPackage, (k, v) -> {
                v.addAll(allClassMethods);
                return v;}) == null)    //if package exists add all methods to defined package
            {
                //compute if package not exists
                packagesMethods.put(classPackage, new HashSet<>(allClassMethods));
            }

        }

        //adding packages as nodes in graph list
        for(PackageDeclaration packageDec : packagesMethods.keySet()) {
            DependencyObj temp = new ModuleDependency(packageDec.getNameAsString());
            temp.setWeight(0);
            moduleDependencies.add(temp);

        }

        //manage connections
        for(CompilationUnit classFile : allRootFiles) {
            //getting info about imports form classFile
            Optional<PackageDeclaration> optionalClassPackage = classFile.getPackageDeclaration();
            if(!optionalClassPackage.isPresent()) continue;
            String classPackageName = classFile.getPackageDeclaration().get().getNameAsString();

            //getting packages used in class file(only from source root)
            List <PackageDeclaration> usedKnownPackages = classFile.getImports()
                    .stream()
                    .map((importDec) -> packagesMethods
                            .keySet()
                            .stream()
                            .filter(k -> importDec.getNameAsString().contains(k.getNameAsString()))
                            .findFirst()
                    ).filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toList());

            if(usedKnownPackages.isEmpty()) continue;   //skip when there isn't known packages
            //getting all class methods
            List<MethodDeclaration> allClassMethods = classFile.findAll(MethodDeclaration.class);

            //connecting nodes
            for(MethodDeclaration methodBody : allClassMethods) {
                List<MethodCallExpr> allMethodCalls = methodBody.findAll(MethodCallExpr.class,
                        (method) -> method.getScope().isPresent());     //method called without object skipped

                for (MethodCallExpr methodCall : allMethodCalls) {
                    for (PackageDeclaration importDec : usedKnownPackages) {
                        //searching methods
                        String searchingMethodName = methodCall.getNameAsString() + "\n" + importDec.getNameAsString();
                        Optional<DependencyObj> foundMethodCount = packagesMethods.get(importDec)
                                .stream()
                                .filter(k -> k.getName().equals(searchingMethodName))
                                .findAny();

                        if (!foundMethodCount.isPresent()) continue; //skip empty

                        //manage connection A -> Packet_A, Packet_A -> Packet_B, Packet_B -> B
                        DependencyObj objA = foundMethodCount.get();
                        DependencyObj packetA = moduleDependencies.get(moduleDependencies.indexOf(
                                new ModuleDependency(importDec.getNameAsString())));
                        DependencyObj packetB = moduleDependencies.get(moduleDependencies.indexOf(
                                new ModuleDependency(classPackageName)));
                        DependencyObj objB = new ModuleDependency(methodBody.getNameAsString() + "\n" + classPackageName);
                        CyclomaticComplexityCalculator calculator = new CyclomaticComplexityCalculator();
                        objB.setCyclomaticComplexity(calculator.computeComplexityForMethod(methodBody));

                        //connection A -> Package_A
                        int aIndexInList = moduleDependencies.indexOf(objA);
                        if (aIndexInList < 0) {
                            objA.setWeight(1);
                            objA.getDependencyList().put(packetA, 1);
                            moduleDependencies.add(objA);
                        } else {
                            objA = moduleDependencies.get(aIndexInList);
                            if (objA.getDependencyList().computeIfPresent(packetA, (k, v) -> v + 1) == null) {
                                objA.getDependencyList().put(packetA, 1);
                            }
                            objA.setWeight(objA.getWeight() + 1);
                        }

                        //connection Package_A -> Package_B
                        if (packetA.getDependencyList().computeIfPresent(packetB, (k, v) -> v + 1) == null) {
                            packetA.getDependencyList().put(packetB, 1);
                        }
                        packetA.setWeight(packetA.getWeight() + 1);

                        //connection Package_B -> B
                        int bIndexInList = moduleDependencies.indexOf(objB);
                        if (bIndexInList < 0) {
                            objB.setWeight(0);
                            packetB.getDependencyList().put(objB, 1);
                            moduleDependencies.add(objB);
                        } else {
                            objB = moduleDependencies.get(bIndexInList);
                            if (packetB.getDependencyList().computeIfPresent(objB, (k, v) -> v + 1) == null) {
                                packetB.getDependencyList().put(objB, 1);
                            }
                        }

                    }
                }
            }
        }

        lastCreatedDependencies = moduleDependencies;
        return moduleDependencies;
    }

    public List<DependencyObj> getMethodsDefinitions(String absolutePath) {
        List<DependencyObj> methodDefinitions = new LinkedList<>();
        SourceRoot root = new SourceRoot(Paths.get(absolutePath));
        List<ParseResult<CompilationUnit>> resultList = root.tryToParseParallelized();

        List<Optional<CompilationUnit>> compilationList =
                resultList.stream().map(ParseResult::getResult).collect(Collectors.toList());
        for (Optional<CompilationUnit> result : compilationList) {
            if (!result.isPresent()) continue; //skip empty object
            for (TypeDeclaration typeDec : result.get().getTypes()) {
                DependencyObj classObject = new MethodDefinitionDependency(typeDec.getNameAsString());
                List<BodyDeclaration> bodyDeclarations = typeDec.getMembers();
                for (BodyDeclaration bodyDeclaration : bodyDeclarations) {
                    if (bodyDeclaration.isMethodDeclaration()) {
                        MethodDeclaration method = (MethodDeclaration) bodyDeclaration;
                        MethodDefinitionDependency methodDefinition = new MethodDefinitionDependency(method.getNameAsString());
                        CyclomaticComplexityCalculator cyclomaticComplexity = new CyclomaticComplexityCalculator();
                        Integer cc = cyclomaticComplexity.computeComplexityForMethod(method);
                        methodDefinition.setCyclomaticComplexity(cc);
                        classObject.addDependency(methodDefinition);
                    }
                }
                methodDefinitions.add(classObject);
            }
        }
        return methodDefinitions;
    }
}
