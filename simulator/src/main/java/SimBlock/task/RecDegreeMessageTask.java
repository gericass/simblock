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

public class RecDegreeMessageTask extends AbstractMessageTask {

	private boolean enableEstimate = false;
	private int kRegion;

	public RecDegreeMessageTask(Node from, Node to) {
		super(from, to);
	}

	@Override
    public long getInterval() {
        return 1;
    }

	public void setEstimatable(int region) {
		this.enableEstimate = true;
		this.kRegion = region;
	}

	public int getKRegion() {
		return kRegion;
	}

	public boolean estimateEnabled() {
		return enableEstimate;
	}

	//public long getInterval(){
	//	return this.interval;
	//}
}
