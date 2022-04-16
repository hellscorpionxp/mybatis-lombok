package org.mybatis.generator.plugins;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
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
    importedTypes.add(new FullyQualifiedJavaType("com.fasterxml.jackson.annotation.JsonFormat"));
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
    if ("ctime".equalsIgnoreCase(field.getName()) || "utime".equalsIgnoreCase(field.getName())
        || "createTime".equalsIgnoreCase(field.getName()) || "updateTime".equalsIgnoreCase(field.getName())) {
      field.addAnnotation("@JsonFormat(pattern = \"yyyy-MM-dd HH:mm:ss\")");
    }
    if (SqlReservedWords.containsWord(introspectedColumn.getActualColumnName())) {
      field.addAnnotation(String.format("@TableField(\"%s%s%s\")", context.getBeginningDelimiter(), field.getName(),
          context.getEndingDelimiter()));
    }
    return true;
  }

}
