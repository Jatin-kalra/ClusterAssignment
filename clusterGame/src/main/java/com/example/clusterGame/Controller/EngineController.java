package com.example.clusterGame.Controller;


import com.example.clusterGame.Model.Request.ContinuePlayRequest;
import com.example.clusterGame.Model.Request.PlayRequest;
import com.example.clusterGame.Model.Response.ContinuePlayResponse;
import com.example.clusterGame.Model.Response.PlayResponse;
import com.example.clusterGame.Service.EngineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ClusterGame")
public class EngineController {

    @Autowired
    EngineService engineService;

    @PostMapping("/play")
    public PlayResponse play(@RequestBody PlayRequest request) throws Exception {

        return engineService.gameRound(request);
    }

    @PostMapping("/continuePlay")
    public ContinuePlayResponse continuePlay(@RequestBody ContinuePlayRequest request) throws Exception {

        return engineService.roundResult(request);
    }
}
