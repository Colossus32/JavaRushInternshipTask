package com.game.service;

import com.game.exceptions.NotFoundException;
import com.game.exceptions.WrongRequestException;
import com.game.controller.PlayerOrder;
import com.game.entity.Player;
import com.game.entity.Profession;
import com.game.entity.Race;
import com.game.repository.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class PlayerService {
    private final PlayerRepository playerRepository;

    @Autowired
    public PlayerService(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    public List<Player> getPlayerWithParameters(String name,
                                                String title,
                                                String race,
                                                String profession,
                                                String afterDate,
                                                String beforeDate,
                                                String banned,
                                                String minExperience,
                                                String maxExperience,
                                                String minLevel,
                                                String maxLevel) {
        List<Player> playerList = playerRepository.findAll();

        //sorting by name (contains symbols)
        if (name != null) playerList = playerList.stream()
                .filter(p -> p.getName().toLowerCase().contains(name.toLowerCase()))
                .collect(Collectors.toList());

        //sorting by title (contains symbols)
        if (title != null) playerList = playerList.stream()
                .filter(p -> p.getTitle().toLowerCase().contains(title.toLowerCase()))
                .collect(Collectors.toList());

        //sorting by race
        if (race != null){
            Race raceSorted = Race.valueOf(race);
            playerList = playerList.stream()
                    .filter(p -> p.getRace() == raceSorted)
                    .collect(Collectors.toList());
        }

        //sorting by profession
        if (profession != null){
            Profession professionSorted = Profession.valueOf(profession);
            playerList = playerList.stream()
                    .filter(p -> p.getProfession() == professionSorted)
                    .collect(Collectors.toList());
        }

        //sorting by date
        if (afterDate != null){
            Date afterDateSorted = new Date(Long.parseLong(afterDate));
            playerList = playerList.stream()
                    .filter(p -> p.getBirthday().after(afterDateSorted))
                    .collect(Collectors.toList());
        }

        if (beforeDate != null){
            Date beforeDateSorted = new Date(Long.parseLong(beforeDate));
            playerList = playerList.stream()
                    .filter(p -> p.getBirthday().before(beforeDateSorted))
                    .collect(Collectors.toList());
        }

        //sorting by banned (is os not)
        if (banned != null){
            boolean isBanned = Boolean.parseBoolean(banned);
            playerList = playerList.stream()
                    .filter(p->p.getBanned() == isBanned)
                    .collect(Collectors.toList());
        }

        //sorting by Experience
        int minExperienceSorted, maxExperienceSorted;

        if (minExperience != null) minExperienceSorted = Integer.parseInt(minExperience);
        else minExperienceSorted = 0;

        if (maxExperience != null) maxExperienceSorted = Integer.parseInt(maxExperience);
        else maxExperienceSorted = 10000000;

        playerList = playerList.stream()
                .filter(p -> p.getExperience() >= minExperienceSorted && p.getExperience() <= maxExperienceSorted)
                .collect(Collectors.toList());

        //sorting by level
        int minLevelSorted, maxLevelSorted;

        if (minLevel != null) minLevelSorted = Integer.parseInt(minLevel);
        else minLevelSorted = 0;

        if (maxLevel != null) maxLevelSorted = Integer.parseInt(maxLevel);
        else maxLevelSorted = Integer.MAX_VALUE;
        
        playerList = playerList.stream()
                .filter(p -> p.getLevel() >= minLevelSorted && p.getLevel() <= maxLevelSorted)
                .collect(Collectors.toList());
        
        return playerList;
    }

    public List<Player> viewer(List<Player> playersList, String pageNumber, String pageSize, String order) {
        int pageCurrent = 0; //default
        if (pageNumber != null) pageCurrent = Integer.parseInt(pageNumber); //if exists

        int playersOnPage = 3; //default
        if (pageSize != null) playersOnPage = Integer.parseInt(pageSize); //if exists

        PlayerOrder playerOrder = PlayerOrder.ID; //default
        if (order != null) playerOrder = PlayerOrder.valueOf(order); //if exists

        //sorting block start------------------------------------------------------------------------

        PlayerOrder finalPlayerOrder = playerOrder;

        Comparator<Player> comparator = (o1, o2) -> {
            switch (finalPlayerOrder) {
                case ID:
                    return (int) (o1.getId() - o2.getId());
                case NAME:
                    return o1.getName().compareTo(o2.getName());
                case EXPERIENCE:
                    return o1.getExperience() - o2.getExperience();
                case BIRTHDAY:
                    return o1.getBirthday().compareTo(o2.getBirthday());
            }
            return 0;
        };

        playersList.sort(comparator);

        //sorting block end--------------------------------------------------------------------------

        int startRange = pageCurrent * playersOnPage;
        int endRange = Math.min(playersOnPage*(pageCurrent + 1), playersList.size());

        return playersList.subList(startRange,endRange);
    }

    public Integer count(String name,
                         String title,
                         String race,
                         String profession,
                         String afterDate,
                         String beforeDate,
                         String banned,
                         String minExperience,
                         String maxExperience,
                         String minLevel,
                         String maxLevel) {

        return getPlayerWithParameters(name,title,race,profession,
                afterDate,beforeDate,banned,minExperience,maxExperience,minLevel,maxLevel)
                .size();
    }

    public Player createPlayer(Map<String, String> body) {
        Player player = validationTest(body);
        playerRepository.saveAndFlush(player);
        return player;
    }

    public Player getPlayer(String id) {
        if (validId(id)){
            return playerRepository.findAll().stream()
                    .filter(p -> p.getId() == Long.parseLong(id))
                    .findAny()
                    .orElseThrow(NotFoundException::new);
        }
        else throw new WrongRequestException();
    }

    public Player updatePlayer(String id, Map<String, String> body) {

        if (!validId(id)) throw new WrongRequestException();

        Player player = getPlayer(id);

        //name
        if (body.containsKey("name")){
            String nameUpdate = body.get("name");
            if (nameValidation(nameUpdate))player.setName(nameUpdate);
            else throw new WrongRequestException();
        }

        //title
        if (body.containsKey("title")) {
            String titleUpdate = body.get("title");
            if(titleUpdate.length() <= 30) player.setTitle(titleUpdate);
            else throw new WrongRequestException();
        }

        //race
        if (body.containsKey("race")) player.setRace(Race.valueOf(body.get("race")));

        //profession
        if (body.containsKey("profession")) player.setProfession(Profession.valueOf(body.get("profession")));

        //birthday
        if (body.containsKey("birthday")){
            long bDay = Long.parseLong(body.get("birthday"));
            if (birthdayValidation(bDay)) player.setBirthday(new Date(bDay));
            else throw new WrongRequestException();
        }

        //banned
        if (body.containsKey("banned")) player.setBanned(Boolean.parseBoolean(body.get("banned")));

        //experience
        if (body.containsKey("experience")){
            int exp = Integer.parseInt(body.get("experience"));
            if (exp >= 0 && exp <= 10000000){
                player.setExperience(exp);
                int lvl = calculateLevel(exp);
                player.setLevel(lvl);
                player.setUntilNextLevel(calculateUntilNextLevel(exp,lvl));
            }
            else throw new WrongRequestException();
        }

        //ID
        player.setId(Long.parseLong(id));

        //save
        playerRepository.save(player);

        return player;
    }

    public void deletePlayer(String id) {
        if (validId(id)) {

            Player player = playerRepository.findAll().stream()
                    .filter(p -> p.getId() == Long.parseLong(id))
                    .findAny()
                    .orElseThrow(NotFoundException::new);

            playerRepository.delete(player);

        }
        else throw new WrongRequestException();
    }

    //utilities-----------------------------------------------------------------------------------

    private Player validationTest(Map<String, String> body) {
        String name = body.get("name");
        String title = body.get("title");
        String race = body.get("race");
        String profession = body.get("profession");
        String birthday = body.get("birthday");
        String banned = body.get("banned");
        String experience = body.get("experience");

        //nulls validation
        if (name != null
                && title != null
                && race != null
                && profession != null
                && birthday != null
                && experience != null)
        {
            Player player = new Player();

            //name <= 12 and is not empty
            if (nameValidation(name)) player.setName(name);
            else throw new WrongRequestException();

            //title <= 30
            if (title.length() <= 30) player.setTitle(title);
            else throw new WrongRequestException();

            //race
            player.setRace(Race.valueOf(race));

            //profession
            player.setProfession(Profession.valueOf(profession));

            //birthday
            long bDay = Long.parseLong(birthday);
            if (birthdayValidation(bDay)) player.setBirthday(new Date(bDay));
            else throw new WrongRequestException();

            //banned
            if (banned != null) player.setBanned(Boolean.parseBoolean(banned));
            else player.setBanned(false);

            //experience , level , experience until next level
            int exp = Integer.parseInt(experience);
            if (exp >= 0 && exp <= 10000000) {
                player.setExperience(exp);
                int currentCalculatedLevel = calculateLevel(exp);
                player.setLevel(currentCalculatedLevel);
                player.setUntilNextLevel(calculateUntilNextLevel(exp,currentCalculatedLevel));
            }
            else throw new WrongRequestException();

            return player;
        }

        throw new WrongRequestException();
    }

    private boolean nameValidation(String name){
        return name.length() <= 12 && !name.trim().equals("");
    }

    private boolean birthdayValidation (Long birthday){

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(birthday);
        Calendar lowerBorder = new GregorianCalendar(2000,0,0);
        Calendar upperBorder = new GregorianCalendar(3000,0,0);

        return calendar.after(lowerBorder) && calendar.before(upperBorder);
    }

    private Integer calculateLevel(int experience){
        return (int)( (Math.sqrt(2500 + 200 * experience) - 50) / 100);
    }

    private Integer calculateUntilNextLevel(int experience, int level){
        return 50 * (level+1) * (level+2) - experience;
    }

    private boolean validId(String id) {
        long validId;
        try{
            validId = Long.parseLong(id);
        }
        catch (NumberFormatException e){
            throw new WrongRequestException();
        }

        return validId > 0;
    }

}
