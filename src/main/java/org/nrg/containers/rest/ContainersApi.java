package org.nrg.containers.rest;

import io.swagger.annotations.*;
import org.nrg.containers.model.*;
import org.nrg.containers.services.ContainerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import java.util.List;

//@Api(description = "XNAT Container Services REST API")
@RestController
@RequestMapping(value = "/containers/running")
public class ContainersApi {
    private static final Logger _log = LoggerFactory.getLogger(ContainersApi.class);

//    @ApiOperation(value = "Get list of images.", notes = "Returns a list of all images on the container server.", response = Image.class, responseContainer = "List")
//    @ApiResponses({
//            @ApiResponse(code = 200, message = "A list of images on the server"),
//            @ApiResponse(code = 500, message = "Unexpected error")})
    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public List<Container> getAllContainers() {
        return service.getAllContainers();
    }

//    @ApiOperation(value = "Gets the container with the specified id.", notes = "Returns the serialized container object with the specified id.", response = Container.class)
//    @ApiResponses({
//            @ApiResponse(code = 200, message = "Container successfully retrieved."),
//            @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
//            @ApiResponse(code = 403, message = "Not authorized to view this container."),
//            @ApiResponse(code = 404, message = "Container not found."),
//            @ApiResponse(code = 500, message = "Unexpected error")})
    @RequestMapping(value = {"/{id}"}, method = RequestMethod.GET)
//    public ResponseEntity<Container> getById(@ApiParam(value = "Id of the container to fetch", required = true) @PathVariable("id") final String id) {
    public ResponseEntity<Container> getById(@ApiParam(value = "Id of the container to fetch", required = true) @PathVariable("id") final String id) {
        final Container container = service.getContainer(id);
        return container == null ? new ResponseEntity<Container>(HttpStatus.NOT_FOUND) : new ResponseEntity<>(container, HttpStatus.OK);
    }

//    @ApiOperation(value = "Gets the image with the specified name.", notes = "Returns the serialized image object with the specified name.", response = String.class)
//    @ApiResponses({
//            @ApiResponse(code = 200, message = "Container successfully retrieved."),
//            @ApiResponse(code = 401, message = "Must be authenticated to access the XNAT REST API."),
//            @ApiResponse(code = 403, message = "Not authorized to view this container."),
//            @ApiResponse(code = 404, message = "Container not found."),
//            @ApiResponse(code = 500, message = "Unexpected error")})
    @RequestMapping(value = {"/{id}/status"}, method = {RequestMethod.GET})
    public ResponseEntity<String> getStatus(@ApiParam(value = "Id of the container to fetch", required = true) @PathVariable("id") final String id) {
        final String status = service.getContainerStatus(id);
        return status == null ? new ResponseEntity<String>(HttpStatus.NOT_FOUND) : new ResponseEntity<>(status, HttpStatus.OK);
    }

    @Inject
    private ContainerService service;
}
