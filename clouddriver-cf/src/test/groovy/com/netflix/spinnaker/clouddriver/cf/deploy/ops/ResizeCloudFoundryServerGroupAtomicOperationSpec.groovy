/*
 * Copyright 2015 Pivotal Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netflix.spinnaker.clouddriver.cf.deploy.ops

import com.netflix.spinnaker.clouddriver.data.task.Task
import com.netflix.spinnaker.clouddriver.data.task.TaskRepository
import com.netflix.spinnaker.clouddriver.cf.TestCredential
import com.netflix.spinnaker.clouddriver.cf.deploy.description.ResizeCloudFoundryServerGroupDescription
import com.netflix.spinnaker.clouddriver.cf.security.TestCloudFoundryClientFactory
import org.cloudfoundry.client.lib.CloudFoundryOperations
import org.cloudfoundry.client.lib.domain.CloudApplication
import org.springframework.web.client.ResourceAccessException
import spock.lang.Specification

class ResizeCloudFoundryServerGroupAtomicOperationSpec extends Specification {

  CloudFoundryOperations client
  CloudFoundryOperations clientForNonExistentServerGroup

  def setup() {
    TaskRepository.threadLocalTask.set(Mock(Task))

    client = Mock(CloudFoundryOperations)

    clientForNonExistentServerGroup = Mock(CloudFoundryOperations)
  }

  void "should not fail resize when server group does not exist"() {
    given:
    1 * clientForNonExistentServerGroup.getApplication("my-stack-v000") >> { throw new ResourceAccessException("app doesn't exist")}
    0 * clientForNonExistentServerGroup._

    def op = new ResizeCloudFoundryServerGroupAtomicOperation(
        new ResizeCloudFoundryServerGroupDescription(
            serverGroupName: "my-stack-v000",
            region: "staging",
            targetSize: 10,
            credentials: TestCredential.named('baz')))
    op.cloudFoundryClientFactory = new TestCloudFoundryClientFactory(stubClient: clientForNonExistentServerGroup)

    when:
    op.operate([])

    then:
    thrown(Exception)
  }

  void "should resize server group (instances only)"() {
    setup:
    def op = new ResizeCloudFoundryServerGroupAtomicOperation(
        new ResizeCloudFoundryServerGroupDescription(
            serverGroupName: "my-stack-v000",
            region: "staging",
            targetSize: 10,
            credentials: TestCredential.named('baz')))
    op.cloudFoundryClientFactory = new TestCloudFoundryClientFactory(stubClient: client)

    def mockApp = Mock(CloudApplication)
    mockApp.instances >> { 1 }
    mockApp.memory >> { 1024 }
    mockApp.diskQuota >> { 1024}


    when:
    op.operate([])

    then:
    1 * client.getApplication("my-stack-v000") >> { mockApp }
    1 * client.updateApplicationInstances("my-stack-v000", 10)
    0 * client._
  }

  void "should resize server group (memory only)"() {
    setup:
    def op = new ResizeCloudFoundryServerGroupAtomicOperation(
        new ResizeCloudFoundryServerGroupDescription(
            serverGroupName: "my-stack-v000",
            region: "staging",
            targetSize: 1,
            memory: 2048,
            credentials: TestCredential.named('baz')))
    op.cloudFoundryClientFactory = new TestCloudFoundryClientFactory(stubClient: client)

    def mockApp = Mock(CloudApplication)
    mockApp.instances >> { 1 }
    mockApp.memory >> { 1024 }
    mockApp.diskQuota >> { 1024}

    when:
    op.operate([])

    then:
    1 * client.getApplication("my-stack-v000") >> { mockApp }
    1 * client.updateApplicationMemory("my-stack-v000", 2048)
    0 * client._
  }

  void "should resize server group (disk only)"() {
    setup:
    def op = new ResizeCloudFoundryServerGroupAtomicOperation(
        new ResizeCloudFoundryServerGroupDescription(
            serverGroupName: "my-stack-v000",
            region: "staging",
            targetSize: 1,
            disk: 2048,
            credentials: TestCredential.named('baz')))
    op.cloudFoundryClientFactory = new TestCloudFoundryClientFactory(stubClient: client)

    def mockApp = Mock(CloudApplication)
    mockApp.instances >> { 1 }
    mockApp.memory >> { 1024 }
    mockApp.diskQuota >> { 1024}

    when:
    op.operate([])

    then:
    1 * client.getApplication("my-stack-v000") >> { mockApp }
    1 * client.updateApplicationDiskQuota("my-stack-v000", 2048)
    0 * client._
  }

}
