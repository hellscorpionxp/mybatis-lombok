package org.mybatis.generator.plugins;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.mybatis.generator.api.GeneratedJavaFile;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.JavaVisibility;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.internal.db.SqlReservedWords;

public class LombokPlugin extends PluginAdapter {

  @Override
  public boolean validate(List<String> warnings) {
    return true;
  }

  @Override
  public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
    Set<FullyQualifiedJavaType> importedTypes = new HashSet<>();
    importedTypes.add(new FullyQualifiedJavaType("lombok.Data"));
    importedTypes.add(new FullyQualifiedJavaType("lombok.EqualsAndHashCode"));
    importedTypes.add(new FullyQualifiedJavaType("lombok.NoArgsConstructor"));
    importedTypes.add(new FullyQualifiedJavaType("lombok.AllArgsConstructor"));
    importedTypes.add(new FullyQualifiedJavaType("lombok.Builder"));
    importedTypes.add(new FullyQualifiedJavaType("com.baomidou.mybatisplus.annotation.TableId"));
    importedTypes.add(new FullyQualifiedJavaType("com.baomidou.mybatisplus.annotation.IdType"));
    importedTypes.add(new FullyQualifiedJavaType("com.baomidou.mybatisplus.annotation.TableField"));
    importedTypes.add(new FullyQualifiedJavaType("com.fasterxml.jackson.annotation.JsonFormat"));
    importedTypes.add(new FullyQualifiedJavaType("io.swagger.annotations.ApiModelProperty"));
    topLevelClass.addImportedTypes(importedTypes);
    topLevelClass.addAnnotation("@Data");
    topLevelClass.addAnnotation("@EqualsAndHashCode");
    topLevelClass.addAnnotation("@NoArgsConstructor");
    topLevelClass.addAnnotation("@AllArgsConstructor");
    topLevelClass.addAnnotation("@Builder");
    return true;
  }

  @Override
  public boolean modelGetterMethodGenerated(Method method, TopLevelClass topLevelClass,
      IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable, ModelClassType modelClassType) {
    return false;
  }

  @Override
  public boolean modelSetterMethodGenerated(Method method, TopLevelClass topLevelClass,
      IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable, ModelClassType modelClassType) {
    return false;
  }

  @Override
  public boolean modelFieldGenerated(Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn,
      IntrospectedTable introspectedTable, ModelClassType modelClassType) {
    if ("id".equalsIgnoreCase(field.getName())) {
      field.addAnnotation("@TableId(type = IdType.AUTO)");
    }
    if ("Date".equals(field.getType().getShortName()) || "LocalDate".equals(field.getType().getShortName())
        || "LocalDateTime".equals(field.getType().getShortName())) {
      field.addAnnotation("@JsonFormat(pattern = \"yyyy-MM-dd HH:mm:ss\")");
    }
    if (SqlReservedWords.containsWord(introspectedColumn.getActualColumnName())) {
      field.addAnnotation(String.format("@TableField(\"%s%s%s\")", context.getBeginningDelimiter(), field.getName(),
          context.getEndingDelimiter()));
    }
    String remarks = introspectedColumn.getRemarks();
    if (remarks != null && !"".equals(remarks)) {
      remarks = remarks.replaceAll("\"", "");
      field.addAnnotation(String.format("@ApiModelProperty(value = \"%s\")", remarks));
    }
    return true;
  }

  @Override
  public boolean clientGenerated(Interface interfaze, IntrospectedTable introspectedTable) {
    String entityName = introspectedTable.getBaseRecordType();
    String shortEntityName = entityName.substring(entityName.lastIndexOf(".") + 1);
    Set<FullyQualifiedJavaType> importedTypes = new HashSet<>();
    importedTypes.add(new FullyQualifiedJavaType("com.baomidou.mybatisplus.core.mapper.BaseMapper"));
    interfaze.addImportedTypes(importedTypes);
    interfaze.addSuperInterface(new FullyQualifiedJavaType(String.format("BaseMapper<%s>", shortEntityName)));
    return true;
  }

  @Override
  public List<GeneratedJavaFile> contextGenerateAdditionalJavaFiles(IntrospectedTable introspectedTable) {
    List<GeneratedJavaFile> generatedJavaFiles = new ArrayList<>();
    generatedJavaFiles.add(generateService(introspectedTable));
    generatedJavaFiles.add(generateController(introspectedTable));
    return generatedJavaFiles;
  }

  private GeneratedJavaFile generateService(IntrospectedTable introspectedTable) {
    String entityName = introspectedTable.getBaseRecordType();
    String shortEntityName = entityName.substring(entityName.lastIndexOf(".") + 1);
    String serviceName = String.format("%s.%sService", properties.getProperty("serviceTargetPackage"), shortEntityName);
    String mapperName = introspectedTable.getMyBatis3JavaMapperType();
    String shortMapperName = mapperName.substring(mapperName.lastIndexOf(".") + 1);
    String mapperFieldName = toCamelCase(shortMapperName);
    FullyQualifiedJavaType type = new FullyQualifiedJavaType(serviceName);
    TopLevelClass clazz = new TopLevelClass(type);
    clazz.setVisibility(JavaVisibility.PUBLIC);
    Set<FullyQualifiedJavaType> importedTypes = new HashSet<>();
    importedTypes.add(new FullyQualifiedJavaType(entityName));
    importedTypes.add(new FullyQualifiedJavaType("com.baomidou.mybatisplus.extension.service.IService"));
    importedTypes.add(new FullyQualifiedJavaType("com.baomidou.mybatisplus.extension.service.impl.ServiceImpl"));
    importedTypes.add(new FullyQualifiedJavaType("org.springframework.stereotype.Service"));
    importedTypes.add(new FullyQualifiedJavaType("lombok.extern.slf4j.Slf4j"));
    importedTypes.add(new FullyQualifiedJavaType("org.springframework.beans.factory.annotation.Autowired"));
    importedTypes.add(new FullyQualifiedJavaType(mapperName));
    clazz.addImportedTypes(importedTypes);
    clazz.addSuperInterface(new FullyQualifiedJavaType(String.format("IService<%s>", shortEntityName)));
    clazz.setSuperClass(String.format("ServiceImpl<%s, %s>", shortMapperName, shortEntityName));
    clazz.addAnnotation("@Service");
    clazz.addAnnotation("@Slf4j");
    Field mapperField = new Field(mapperFieldName, new FullyQualifiedJavaType(mapperName));
    mapperField.setVisibility(JavaVisibility.PRIVATE);
    mapperField.addAnnotation("@Autowired");
    clazz.addField(mapperField);
    return new GeneratedJavaFile(clazz, (String) properties.get("serviceTargetProject"), "UTF-8",
        context.getJavaFormatter());
  }

  private GeneratedJavaFile generateController(IntrospectedTable introspectedTable) {
    String entityName = introspectedTable.getBaseRecordType();
    String shortEntityName = entityName.substring(entityName.lastIndexOf(".") + 1);
    String controllerName = String.format("%s.%sController", properties.getProperty("controllerTargetPackage"),
        shortEntityName);
    String serviceName = String.format("%s.%sService", properties.getProperty("serviceTargetPackage"), shortEntityName);
    String shortServiceName = serviceName.substring(serviceName.lastIndexOf(".") + 1);
    String serviceFieldName = toCamelCase(shortServiceName);
    FullyQualifiedJavaType type = new FullyQualifiedJavaType(controllerName);
    TopLevelClass clazz = new TopLevelClass(type);
    clazz.setVisibility(JavaVisibility.PUBLIC);
    Set<FullyQualifiedJavaType> importedTypes = new HashSet<>();
    importedTypes.add(new FullyQualifiedJavaType(entityName));
    importedTypes.add(new FullyQualifiedJavaType("org.springframework.web.bind.annotation.RestController"));
    importedTypes.add(new FullyQualifiedJavaType("org.springframework.web.bind.annotation.RequestMapping"));
    importedTypes.add(new FullyQualifiedJavaType("io.swagger.annotations.Api"));
    importedTypes.add(new FullyQualifiedJavaType("lombok.extern.slf4j.Slf4j"));
    importedTypes.add(new FullyQualifiedJavaType("org.springframework.beans.factory.annotation.Autowired"));
    importedTypes.add(new FullyQualifiedJavaType(serviceName));
    clazz.addImportedTypes(importedTypes);
    clazz.addAnnotation("@RestController");
    clazz.addAnnotation("@RequestMapping(value = \"/\")");
    String remarks = introspectedTable.getRemarks();
    if (remarks != null && !"".equals(remarks)) {
      remarks = remarks.replaceAll("\"", "");
      clazz.addAnnotation(String.format("@Api(tags = \"%s\")", remarks));
    }
    clazz.addAnnotation("@Slf4j");
    Field serviceField = new Field(serviceFieldName, new FullyQualifiedJavaType(serviceName));
    serviceField.setVisibility(JavaVisibility.PRIVATE);
    serviceField.addAnnotation("@Autowired");
    clazz.addField(serviceField);
    return new GeneratedJavaFile(clazz, (String) properties.get("controllerTargetProject"), "UTF-8",
        context.getJavaFormatter());
  }

  private String toCamelCase(String str) {
    char[] array1 = str.toCharArray();
    char[] array2 = str.toLowerCase().toCharArray();
    array1[0] = array2[0];
    return new String(array1);
  }

}
