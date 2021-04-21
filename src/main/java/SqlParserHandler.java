import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.alter.Alter;
import net.sf.jsqlparser.statement.create.index.CreateIndex;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.create.view.CreateView;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.drop.Drop;
import net.sf.jsqlparser.statement.execute.Execute;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.merge.Merge;
import net.sf.jsqlparser.statement.replace.Replace;
import net.sf.jsqlparser.statement.select.*;
import net.sf.jsqlparser.statement.truncate.Truncate;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.statement.upsert.Upsert;
import net.sf.jsqlparser.util.TablesNamesFinder;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class SqlParserHandler {

  /**
   * 由于jsqlparser没有获取SQL类型的原始工具，并且在下面操作时需要知道SQL类型，所以编写此工具方法
   *
   * @param sql sql语句
   * @return sql类型，
   * @throws JSQLParserException
   */
  public String getSqlType(String sql) throws JSQLParserException {
    Statement sqlStmt = CCJSqlParserUtil.parse(new StringReader(sql));
    if (sqlStmt instanceof Alter) {
      return "ALTER";
    } else if (sqlStmt instanceof CreateIndex) {
      return "CREATEINDEX";
    } else if (sqlStmt instanceof CreateTable) {
      return "CREATETABLE";
    } else if (sqlStmt instanceof CreateView) {
      return "CREATEVIEW";
    } else if (sqlStmt instanceof Delete) {
      return "DELETE";
    } else if (sqlStmt instanceof Drop) {
      return "DROP";
    } else if (sqlStmt instanceof Execute) {
      return "EXECUTE";
    } else if (sqlStmt instanceof Insert) {
      return "INSERT";
    } else if (sqlStmt instanceof Merge) {
      return "MERGE";
    } else if (sqlStmt instanceof Replace) {
      return "REPLACE";
    } else if (sqlStmt instanceof Select) {
      return "SELECT";
    } else if (sqlStmt instanceof Truncate) {
      return "TRUNCATE";
    } else if (sqlStmt instanceof Update) {
      return "UPDATE";
    } else if (sqlStmt instanceof Upsert) {
      return "UPSERT";
    } else {
      return "NONE";
    }
  }

  /**
   * 获取sql操作接口,与上面类型判断结合使用
   * example:
   * String sql = "create table a(a string)";
   * SqlType sqlType = SqlParserTool.getSqlType(sql);
   * if(sqlType.equals(SqlType.SELECT)){
   * Select statement = (Select) SqlParserTool.getStatement(sql);
   * }
   *
   * @param sql
   * @return
   * @throws JSQLParserException
   */
  public Statement getStatement(String sql) throws JSQLParserException {
    Statement sqlStmt = CCJSqlParserUtil.parse(new StringReader(sql));
    return sqlStmt;
  }

  /**
   * 获取tables的表名
   *
   * @param statement
   * @return
   */
  public <T> List<String> getTableList(T statement) {
    TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
    List<String> tableList = tablesNamesFinder.getTableList((Statement) statement);
    return tableList;
  }

  /**
   * 获取join层级
   *
   * @param selectBody
   * @return
   */
  public List<Join> getJoins(SelectBody selectBody) {
    if (selectBody instanceof PlainSelect) {
      List<Join> joins = ((PlainSelect) selectBody).getJoins();
      return joins;
    }
    return new ArrayList<Join>();
  }

  /**
   * @param selectBody
   * @return
   */
  public List<Table> getIntoTables(SelectBody selectBody) {
    if (selectBody instanceof PlainSelect) {
      List<Table> tables = ((PlainSelect) selectBody).getIntoTables();
      return tables;
    }
    return new ArrayList<Table>();
  }

  /**
   * @param selectBody
   * @return
   */
  public void setIntoTables(SelectBody selectBody, List<Table> tables) {
    if (selectBody instanceof PlainSelect) {
      ((PlainSelect) selectBody).setIntoTables(tables);
    }
  }

  /**
   * 获取limit值
   *
   * @param selectBody
   * @return
   */
  public Limit getLimit(SelectBody selectBody) {
    if (selectBody instanceof PlainSelect) {
      Limit limit = ((PlainSelect) selectBody).getLimit();
      return limit;
    }
    return null;
  }

  /**
   * 为SQL增加limit值
   *
   * @param selectBody
   * @param l
   */
  public void setLimit(SelectBody selectBody, long l) {
    if (selectBody instanceof PlainSelect) {
      Limit limit = new Limit();
      limit.setRowCount(new LongValue(String.valueOf(l)));
      ((PlainSelect) selectBody).setLimit(limit);
    }
  }

  /**
   * 获取FromItem不支持子查询操作
   *
   * @param selectBody
   * @return
   */
  public FromItem getFromItem(SelectBody selectBody) {
    if (selectBody instanceof PlainSelect) {
      FromItem fromItem = ((PlainSelect) selectBody).getFromItem();
      return fromItem;
    } else if (selectBody instanceof WithItem) {
      getFromItem(((WithItem) selectBody).getSelectBody());
    }
    return null;
  }

  /**
   * 获取子查询
   *
   * @param selectBody
   * @return
   */
  public SubSelect getSubSelect(SelectBody selectBody) {
    if (selectBody instanceof PlainSelect) {
      FromItem fromItem = ((PlainSelect) selectBody).getFromItem();
      if (fromItem instanceof SubSelect) {
        return ((SubSelect) fromItem);
      }
    } else if (selectBody instanceof WithItem) {
      getSubSelect(((WithItem) selectBody).getSelectBody());
    }
    return null;
  }

  /**
   * 判断是否为多级子查询
   *
   * @param selectBody
   * @return
   */
  public boolean isMultiSubSelect(SelectBody selectBody) {
    if (selectBody instanceof PlainSelect) {
      FromItem fromItem = ((PlainSelect) selectBody).getFromItem();
      if (fromItem instanceof SubSelect) {
        SelectBody subBody = ((SubSelect) fromItem).getSelectBody();
        if (subBody instanceof PlainSelect) {
          FromItem subFromItem = ((PlainSelect) subBody).getFromItem();
          if (subFromItem instanceof SubSelect) {
            return true;
          }
        }
      }
    }
    return false;
  }

  /**
   * 获取查询字段
   *
   * @param selectBody
   * @return
   */
  public List<SelectItem> getSelectItems(SelectBody selectBody) {
    if (selectBody instanceof PlainSelect) {
      List<SelectItem> selectItems = ((PlainSelect) selectBody).getSelectItems();
      return selectItems;
    }
    return null;
  }

  public void main(String[] args) throws JSQLParserException {
    String sql = "select count(1) from IAP_AUDIT a where 1=1                                                                          AND               SCENE_ID = :1                                                                                                                     and           AUDIT_STATUS = '5'                                                               AND          exists (select 1 from IAP_AUDIT_SCOPE_MAIN_ACCT t where t.value = a.org_id and t.MAIN_ACCT_ID = :2)                           AND                          CREATE_TIME >= to_date(:3, 'yyyy-MM')           and CREATE_TIME < add_months(to_date(:4, 'yyyy-MM'), 1)             ";

    Statement statement = getStatement(sql);
    List<String> tableList = getTableList(statement);
    String sqlType = getSqlType(sql);
    System.out.println(sqlType);
    for (String s : tableList) {
      System.out.println(s);
    }
//            SubSelect subSelect = SqlParserTool.getSubSelect(statement.getSelectBody());
//            System.out.println(subSelect.getSelectBody());

  }

}
