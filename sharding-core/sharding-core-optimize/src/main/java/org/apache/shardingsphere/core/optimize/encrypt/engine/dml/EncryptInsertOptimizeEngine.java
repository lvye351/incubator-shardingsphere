/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.core.optimize.encrypt.engine.dml;

import org.apache.shardingsphere.core.metadata.table.TableMetas;
import org.apache.shardingsphere.core.optimize.api.segment.InsertValue;
import org.apache.shardingsphere.core.optimize.encrypt.engine.EncryptOptimizeEngine;
import org.apache.shardingsphere.core.optimize.encrypt.statement.EncryptInsertOptimizedStatement;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.core.parse.sql.statement.dml.InsertStatement;
import org.apache.shardingsphere.core.rule.EncryptRule;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Insert optimize engine for encrypt.
 *
 * @author panjuan
 */
public final class EncryptInsertOptimizeEngine implements EncryptOptimizeEngine<InsertStatement> {
    
    @Override
    public EncryptInsertOptimizedStatement optimize(final EncryptRule encryptRule, final TableMetas tableMetas, final String sql, final List<Object> parameters, final InsertStatement sqlStatement) {
        EncryptInsertOptimizedStatement result = new EncryptInsertOptimizedStatement(sqlStatement, tableMetas);
        int parametersOffset = 0;
        Collection<String> encryptDerivedColumnNames = encryptRule.getAssistedQueryAndPlainColumns(sqlStatement.getTable().getTableName());
        Collection<String> columnNames = sqlStatement.useDefaultColumns() ? tableMetas.getAllColumnNames(sqlStatement.getTable().getTableName()) : sqlStatement.getColumnNames();
        int derivedColumnCount = encryptRule.getAssistedQueryAndPlainColumnCount(sqlStatement.getTable().getTableName());
        for (Collection<ExpressionSegment> each : sqlStatement.getAllValueExpressions()) {
            InsertValue insertValue = createInsertValue(columnNames, encryptDerivedColumnNames, each, derivedColumnCount, parameters, parametersOffset);
            result.getInsertValues().add(insertValue);
            Object[] currentParameters = insertValue.getParameters();
            if (encryptRule.containsQueryAssistedColumn(sqlStatement.getTable().getTableName())) {
                fillAssistedQueryInsertValue(encryptRule, Arrays.asList(currentParameters), sqlStatement.getTable().getTableName(), columnNames, insertValue);
            }
            if (encryptRule.containsPlainColumn(sqlStatement.getTable().getTableName())) {
                fillPlainInsertValue(encryptRule, Arrays.asList(currentParameters), sqlStatement.getTable().getTableName(), columnNames, insertValue);
            }
            parametersOffset += insertValue.getParametersCount();
        }
        return result;
    }
    
    private InsertValue createInsertValue(final Collection<String> columnNames, final Collection<String> derivedColumnNames, final Collection<ExpressionSegment> assignments,
                                          final int derivedColumnsCount, final List<Object> parameters, final int parametersOffset) {
        List<String> allColumnNames = new LinkedList<>(columnNames);
        allColumnNames.addAll(derivedColumnNames);
        return new InsertValue(allColumnNames, assignments, derivedColumnsCount, parameters, parametersOffset);
    }
    
    private void fillAssistedQueryInsertValue(final EncryptRule encryptRule, 
                                              final List<Object> parameters, final String tableName, final Collection<String> columnNames, final InsertValue insertValue) {
        for (String each : columnNames) {
            if (encryptRule.isLogicColumn(tableName, each) || encryptRule.isCipherColumn(tableName, each)) {
                String logicColumn = encryptRule.isLogicColumn(tableName, each) ? each : encryptRule.getLogicColumn(tableName, each);
                if (encryptRule.getAssistedQueryColumn(tableName, logicColumn).isPresent()) {
                    insertValue.appendValue((Comparable<?>) insertValue.getValue(logicColumn), parameters);
                }
            }
        }
    }
    
    private void fillPlainInsertValue(final EncryptRule encryptRule, final List<Object> parameters, final String tableName, final Collection<String> columnNames, final InsertValue insertValue) {
        for (String each : columnNames) {
            if (encryptRule.isLogicColumn(tableName, each) || encryptRule.isCipherColumn(tableName, each)) {
                String logicColumn = encryptRule.isLogicColumn(tableName, each) ? each : encryptRule.getLogicColumn(tableName, each);
                if (encryptRule.getPlainColumn(tableName, logicColumn).isPresent()) {
                    insertValue.appendValue((Comparable<?>) insertValue.getValue(logicColumn), parameters);
                }
            }
        }
    }
}
