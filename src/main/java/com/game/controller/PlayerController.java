package com.game.controller;

import com.game.entity.Player;
import com.game.service.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("rest/players")
public class PlayerController {

    private final PlayerService playerService;

    @Autowired
    public PlayerController(PlayerService playerService) {
        this.playerService = playerService;
    }

    @GetMapping
    public List<Player>getPlayersWithParameters(
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "race", required = false) String race,
            @RequestParam(value = "profession", required = false) String profession,
            @RequestParam(value = "after", required = false) String afterDate,
            @RequestParam(value = "before", required = false) String beforeDate,
            @RequestParam(value = "banned", required = false) String banned,
            @RequestParam(value = "minExperience", required = false) String minExperience,
            @RequestParam(value = "maxExperience", required = false) String maxExperience,
            @RequestParam(value = "minLevel", required = false) String minLevel,
            @RequestParam(value = "maxLevel", required = false) String maxLevel,
            @RequestParam(value = "pageNumber", required = false) String pageNumber,
            @RequestParam(value = "pageSize", required = false) String pageSize,
            @RequestParam(value = "order", required = false) String order
    ){
        List<Player> playersList = playerService.getPlayerWithParameters(name,title,race,profession,
                afterDate,beforeDate,banned,minExperience,maxExperience,minLevel,maxLevel);
        return playerService.viewer(playersList, pageNumber,pageSize,order);
    }

    @GetMapping("count")
    public Integer count(
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "race", required = false) String race,
            @RequestParam(value = "profession", required = false) String profession,
            @RequestParam(value = "after", required = false) String afterDate,
            @RequestParam(value = "before", required = false) String beforeDate,
            @RequestParam(value = "banned", required = false) String banned,
            @RequestParam(value = "minExperience", required = false) String minExperience,
            @RequestParam(value = "maxExperience", required = false) String maxExperience,
            @RequestParam(value = "minLevel", required = false) String minLevel,
            @RequestParam(value = "maxLevel", required = false) String maxLevel
    ){
        return playerService.count(name,title,race,profession,
                afterDate,beforeDate,banned,minExperience,maxExperience,minLevel,maxLevel);
    }

    @PostMapping
    public Player createPlayer(@RequestBody Map<String,String> body){
        return playerService.createPlayer(body);
    }

    @GetMapping("{id}")
    public Player getPlayer(@PathVariable ("id") String id){
        return playerService.getPlayer(id);
    }

    @PostMapping("{id}")
    public Player updatePlayer(@PathVariable ("id") String id, @RequestBody Map<String,String> body){
        return playerService.updatePlayer(id,body);
    }

    @DeleteMapping("{id}")
    public void deletePlayer(@PathVariable("id") String id){
        playerService.deletePlayer(id);
    }

}
