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
package org.joyqueue.client.internal.trace.support;

import com.google.common.collect.Lists;
import org.joyqueue.client.internal.trace.Trace;
import org.joyqueue.client.internal.trace.TraceCaller;
import org.joyqueue.client.internal.trace.TraceContext;

import java.util.List;

/**
 * CompositeTrace
 *
 * author: gaohaoxiang
 * date: 2019/1/3
 */
public class CompositeTrace implements Trace {

    private List<Trace> traces;

    public CompositeTrace(List<Trace> traces) {
        this.traces = traces;
    }

    @Override
    public TraceCaller begin(TraceContext context) {
        List<TraceCaller> callers = Lists.newLinkedList();
        for (Trace trace : traces) {
            TraceCaller caller = trace.begin(context);
            callers.add(caller);
        }
        return new CompositeTraceCaller(callers);
    }

    public List<Trace> getTraces() {
        return traces;
    }

    @Override
    public String type() {
        return "composite";
    }
}