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
package com.netflix.spinnaker.clouddriver.cf.deploy.validators

import com.netflix.spinnaker.clouddriver.cf.CloudFoundryOperation
import com.netflix.spinnaker.clouddriver.deploy.DescriptionValidator
import com.netflix.spinnaker.clouddriver.orchestration.AtomicOperations
import com.netflix.spinnaker.clouddriver.security.AccountCredentialsProvider
import com.netflix.spinnaker.clouddriver.cf.deploy.description.ResizeCloudFoundryServerGroupDescription
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.validation.Errors

@CloudFoundryOperation(AtomicOperations.RESIZE_SERVER_GROUP)
@Component("resizeCloudFoundryServerGroupDescriptionValidator")
class ResizeCloudFoundryServerGroupDescriptionValidator extends DescriptionValidator<ResizeCloudFoundryServerGroupDescription> {

  @Autowired
  AccountCredentialsProvider accountCredentialsProvider

  @Override
  void validate(List priorDescriptions, ResizeCloudFoundryServerGroupDescription description, Errors errors) {
    def helper = new StandardCfAttributeValidator("resizeCloudFoundryServerGroupDescription", errors)

    helper.validateCredentials(description.accountName, accountCredentialsProvider)
    helper.validateServerGroupName(description.serverGroupName)
    helper.validateRegion(description.region)
    helper.validatePositiveInt(description.targetSize, "targetSize")
    helper.validatePositiveInt(description.memory, "memory")
    helper.validatePositiveInt(description.disk, "disk")
  }
}
