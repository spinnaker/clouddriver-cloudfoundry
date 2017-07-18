/*
 * Copyright 2015 Pivotal, Inc.
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

package com.netflix.spinnaker.clouddriver.cf.deploy.converters
import com.fasterxml.jackson.databind.ObjectMapper
import com.netflix.spinnaker.clouddriver.cf.security.CloudFoundryAccountCredentials
import com.netflix.spinnaker.clouddriver.security.AccountCredentialsProvider
import com.netflix.spinnaker.clouddriver.cf.deploy.description.ResizeCloudFoundryServerGroupDescription
import com.netflix.spinnaker.clouddriver.cf.deploy.ops.ResizeCloudFoundryServerGroupAtomicOperation
import spock.lang.Shared
import spock.lang.Specification

class ResizeCloudFoundryServerGroupAtomicOperationConverterSpec extends Specification {

  @Shared
  ObjectMapper mapper = new ObjectMapper()

  @Shared
  ResizeCloudFoundryServerGroupAtomicOperationConverter converter

  def setupSpec() {
    def accountCredentialsProvider = Stub(AccountCredentialsProvider) {
      getCredentials('test') >> Stub(CloudFoundryAccountCredentials)
    }
    converter = new ResizeCloudFoundryServerGroupAtomicOperationConverter(objectMapper: mapper,
        accountCredentialsProvider: accountCredentialsProvider
    )
  }

  def "should return ResizeCloudFoundryServerGroupDescription and ResizeCloudFoundryServerGroupAtomicOperation"() {
    setup:
    def input = [serverGroupName: 'demo-staging-v001', region: 'some-region',
                 targetSize     : 1,
                 credentials    : 'test']

    when:
    def description = converter.convertDescription(input)

    then:
    description instanceof ResizeCloudFoundryServerGroupDescription

    when:
    def operation = converter.convertOperation(input)

    then:
    operation instanceof ResizeCloudFoundryServerGroupAtomicOperation
  }

  void "should not fail to serialize unknown properties"() {
    setup:
    def serverGroup = "demo-staging-v001"
    def region = 'some-region'
    def targetSize = 1
    def input = [serverGroupName: serverGroup, region: region,
                 targetSize     : targetSize,
                 unknownProp    : "this",
                 credentials    : 'test']

    when:
    def description = converter.convertDescription(input)

    then:
    description.serverGroupName == serverGroup
    description.targetSize == targetSize
    description.region == region
    description.memory == 1024
    description.disk == 1024
  }

  void "should handle capacity in lieu of targetSize"() {
    setup:
    def serverGroup = "demo-staging-v001"
    def region = 'some-region'
    def capacity = [min: 1, desired: 5, max: 10]
    def input = [serverGroupName: serverGroup, region: region,
                 capacity       : capacity,
                 unknownProp    : "this",
                 credentials    : 'test']

    when:
    def description = converter.convertDescription(input)

    then:
    description.serverGroupName == serverGroup
    description.targetSize == capacity.desired
    description.region == region
  }

  void "should handle memory/disk settings"() {
    setup:
    def serverGroup = "demo-staging-v001"
    def region = 'some-region'
    def capacity = [min: 1, desired: 5, max: 10]
    def input = [
        serverGroupName: serverGroup,
        region: region,
        capacity: capacity,
        memory: 2048,
        disk: 4096,
        credentials    : 'test']

    when:
    def description = converter.convertDescription(input)

    then:
    description.serverGroupName == serverGroup
    description.region == region
    description.memory == 2048
    description.disk == 4096
  }

}
