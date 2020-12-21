/**
 * Copyright 2019 The JoyQueue Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.joyqueue.model;

/**
 * 分页查询条件
 * Created by yangyang115 on 18-7-26.
 */
public class QPageQuery<Q extends Query> extends ListQuery<Q> {

    private Pagination pagination;


    public QPageQuery() {
    }

    public QPageQuery(Pagination pagination, Q query) {
        super(query);
        this.pagination = pagination;
    }

    public Pagination getPagination() {
        return pagination;
    }

    public void setPagination(Pagination pagination) {
        this.pagination = pagination;
    }


    @Override
    public String toString() {
        return "QPageQuery{" +
                "pagination=" + pagination +
                ", query=" + query +
                '}';
    }
}
