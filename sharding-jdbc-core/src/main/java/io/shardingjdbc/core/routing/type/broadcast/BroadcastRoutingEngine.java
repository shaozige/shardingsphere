/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingjdbc.core.routing.type.broadcast;

import com.google.common.base.Preconditions;
import io.shardingjdbc.core.parsing.parser.sql.SQLStatement;
import io.shardingjdbc.core.parsing.parser.sql.ddl.DDLStatement;
import io.shardingjdbc.core.parsing.parser.token.IndexToken;
import io.shardingjdbc.core.routing.type.RoutingEngine;
import io.shardingjdbc.core.routing.type.RoutingResult;
import io.shardingjdbc.core.routing.type.TableUnit;
import io.shardingjdbc.core.rule.DataNode;
import io.shardingjdbc.core.rule.ShardingRule;
import io.shardingjdbc.core.rule.TableRule;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

/**
 * Broadcast routing engine.
 * 
 * @author zhangliang
 */
@RequiredArgsConstructor
public final class BroadcastRoutingEngine implements RoutingEngine {
    
    private final ShardingRule shardingRule;
    
    private final SQLStatement sqlStatement;
    
    @Override
    public RoutingResult route() {
        RoutingResult result = new RoutingResult();
        for (String each : getLogicTableNames()) {
            result.getTableUnits().getTableUnits().addAll(getAllTableUnits(each));
        }
        return result;
    }
    
    private Collection<String> getLogicTableNames() {
        if (isOperateIndexWithoutTable()) {
            return Collections.singletonList(shardingRule.getLogicTableName(getIndexToken().getIndexName()));
        }
        return sqlStatement.getTables().getTableNames();
    }
    
    private boolean isOperateIndexWithoutTable() {
        return sqlStatement instanceof DDLStatement && sqlStatement.getTables().isEmpty();
    }
    
    private IndexToken getIndexToken() {
        Preconditions.checkState(1 == sqlStatement.getSqlTokens().size());
        return (IndexToken) sqlStatement.getSqlTokens().get(0);
    }
    
    private Collection<TableUnit> getAllTableUnits(final String logicTableName) {
        Collection<TableUnit> result = new LinkedList<>();
        TableRule tableRule = shardingRule.getTableRule(logicTableName);
        for (DataNode each : tableRule.getActualDataNodes()) {
            result.add(new TableUnit(each.getDataSourceName(), logicTableName, each.getTableName()));
        }
        return result;
    }
}
