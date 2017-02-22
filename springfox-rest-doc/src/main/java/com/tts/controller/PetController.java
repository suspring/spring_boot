/*
 *
 *  Copyright 2015 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *
 */

package com.tts.controller;

import com.tts.Responses;
import com.tts.model.Pet;
import com.tts.model.Pets;
import com.tts.repository.MapBackedRepository;
import io.swagger.annotations.*;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_XML_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
@RequestMapping(value = "/pets", produces = {APPLICATION_JSON_VALUE, APPLICATION_XML_VALUE, "application/x-smile"})
@Api(value = "/pets", tags = "Pets", description = "Operations about pets")
public class PetController {

  PetRepository petData = new PetRepository();

  @RequestMapping(value = "/{petId}", method = GET)
  @ApiOperation(
          value = "Find pet by ID", notes = "Returns a pet when ID < 10. ID > 10 or nonintegers will simulate API " +
          "error conditions",
          response = Pet.class,
          authorizations = {
                  @Authorization(value = "api_key"),
                  @Authorization(value = "petstore_auth", scopes = {
                          @AuthorizationScope(scope = "write_pets", description = ""),
                          @AuthorizationScope(scope = "read_pets", description = "")
                  })})
  @ApiResponses(value = {
          @ApiResponse(code = 400, message = "Invalid ID supplied"),
          @ApiResponse(code = 404, message = "Pet not found")}
  )
  public ResponseEntity<Pet> getPetById(
          @ApiParam(value = "ID of pet that needs to be fetched", allowableValues = "range[1,5]", required = true)
          @PathVariable("petId") String petId)
          throws NotFoundException {
    Pet pet = petData.get(Long.valueOf(petId));
      System.out.println(pet.getId() + " " + pet.getName());
    if (null != pet) {
      return Responses.ok(pet);
    } else {
      throw new NotFoundException(404, "Pet not found");
    }
  }

  @RequestMapping(method = POST)
  @ApiOperation(value = "Add a new pet to the store")
  @ApiResponses(value = {@ApiResponse(code = 405, message = "Invalid input")})
  public ResponseEntity<String> addPet(
          @ApiParam(value = "Pet object that needs to be added to the store", required = true) @RequestBody Pet pet) {
    petData.add(pet);
    return Responses.ok("SUCCESS");
  }

  @RequestMapping(method = PUT)
  @ApiOperation(value = "Update an existing pet",
          authorizations = @Authorization(value = "petstore_auth", scopes = {
                  @AuthorizationScope(scope = "write_pets", description = ""),
                  @AuthorizationScope(scope = "read_pets", description = "")
          }))
  @ApiResponses(value = {@ApiResponse(code = 400, message = "Invalid ID supplied"),
          @ApiResponse(code = 404, message = "Pet not found"),
          @ApiResponse(code = 405, message = "Validation exception")})
  public ResponseEntity<String> updatePet(
          @ApiParam(value = "Pet object that needs to be added to the store", required = true) @RequestBody Pet pet) {
    petData.add(pet);
    return Responses.ok("SUCCESS");
  }

  @RequestMapping(value = "/findByStatus", method = GET)
  @ApiOperation(
          value = "Finds Pets by status",
          notes = "Multiple status values can be provided with comma seperated strings",
          response = Pet.class,
          responseContainer = "List",
          authorizations = @Authorization(value = "petstore_auth", scopes = {
                  @AuthorizationScope(scope = "write_pets", description = ""),
                  @AuthorizationScope(scope = "read_pets", description = "")
          }))
  @ApiResponses(value = {@ApiResponse(code = 400, message = "Invalid status value")})
  /** TODO: This renders parameter as
   *
   "name": "status",
   "in": "query",
   "description": "Status values that need to be considered for filter",
   "required": false,
   "type": "array",
   "items": {"type": "string"},
   "collectionFormat": "multi",
   "default": "available"
   */
  public ResponseEntity<List<Pet>> findPetsByStatus(
          @ApiParam(value = "Status values that need to be considered for filter",
                  required = true,
                  defaultValue = "available",
                  allowableValues = "available,pending,sold",
                  allowMultiple = true)
          @RequestParam("status") String status) {
    return Responses.ok(petData.findPetByStatus(status));
  }

  @RequestMapping(value = "/findByTags", method = GET)
  @ApiOperation(
          value = "Finds Pets by tags",
          notes = "Muliple tags can be provided with comma seperated strings. Use tag1, tag2, tag3 for testing.",
          response = Pet.class,
          responseContainer = "List",
          authorizations = @Authorization(value = "petstore_auth", scopes = {
                  @AuthorizationScope(scope = "write_pets", description = ""),
                  @AuthorizationScope(scope = "read_pets", description = "")
          }))
  @ApiResponses(value = {@ApiResponse(code = 400, message = "Invalid tag value")})
  @Deprecated
  /** TODO: This renders the parameter as 
  "name": "tags",
          "in": "query",
          "description": "Tags to filter by",
          "required": false,
          "type": "array",
          "items": {"type": "string"},
          "collectionFormat": "multi" */
  public ResponseEntity<List<Pet>> findPetsByTags(
          @ApiParam(
                  value = "Tags to filter by",
                  required = true,
                  allowMultiple = true)
          @RequestParam("tags") String tags) {
    return Responses.ok(petData.findPetByTags(tags));
  }

  static class PetRepository extends MapBackedRepository<Long, Pet> {
    public List<Pet> findPetByStatus(String status) {
      return where(Pets.statusIs(status));
    }

    public List<Pet> findPetByTags(String tags) {
      return where(Pets.tagsContain(tags));
    }
  }
}
