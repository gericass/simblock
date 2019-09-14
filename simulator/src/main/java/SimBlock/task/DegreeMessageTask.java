/**
 * Copyright 2019 Distributed Systems Group
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package SimBlock.task;

import SimBlock.node.Node;

public class DegreeMessageTask extends AbstractMessageTask {

    private int degree;
    private Long estimateCost = null;

	public DegreeMessageTask(Node from, Node to, int degree) {
        super(from, to);
        this.degree = degree;
    }
    
    @Override
    public long getInterval() {
        return 1;
    }

    public void setEstimateCost(Long cost) {
        this.estimateCost = cost;
    }

    public Long getEstimateCost() {
        return this.estimateCost;
    }

    public int getDegree() {
        return this.degree;
    }
}
