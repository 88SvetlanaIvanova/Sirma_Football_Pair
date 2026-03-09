package com.sirma.football.repositorytest;

import com.sirma.football.entities.Player;
import com.sirma.football.entities.Team;
import com.sirma.football.repositories.PlayerRepository;
import com.sirma.football.repositories.TeamRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PlayerRepositoryTest {

    @Autowired PlayerRepository playerRepository;
    @Autowired TeamRepository teamRepository;

    private Team team(String name, String manager, String group) {
        Team t = new Team();
        t.setName(name);
        t.setManagerFullName(manager);
        t.setGroupName(group);
        return t;
    }

    private Player player(Long legacyId, int number, String position, String fullName, Team team) {
        Player p = new Player();
        p.setLegacyId(legacyId);
        p.setTeamNumber(number);
        p.setPosition(position);
        p.setFullName(fullName);
        p.setTeam(team);
        return p;
    }

    @Test
    @DisplayName("findByTeamId returns only players from the requested team")
    void findByTeamId_returnsPlayersOfTeam() {
        // given
        Team spain = teamRepository.saveAndFlush(team("Spain", "Luis de la Fuente", "A"));
        Team france = teamRepository.saveAndFlush(team("France", "Didier Deschamps", "B"));

        Player p1 = playerRepository.saveAndFlush(player(1001L, 7, "FW", "Alvaro Morata", spain));
        Player p2 = playerRepository.saveAndFlush(player(1002L, 10, "MF", "Pedri", spain));
        Player p3 = playerRepository.saveAndFlush(player(2001L, 10, "FW", "Kylian Mbappé", france));

        List<Player> spainPlayers = playerRepository.findByTeamId(spain.getId());

        assertThat(spainPlayers)
                .hasSize(2)
                .extracting(Player::getLegacyId)
                .containsExactlyInAnyOrder(p1.getLegacyId(), p2.getLegacyId())
                .doesNotContain(p3.getLegacyId());
    }

    @Test
    @DisplayName("findByLegacyId returns the correct player when present")
    void findByLegacyId_returnsCorrectPlayer() {
        Team italy = teamRepository.saveAndFlush(team("Italy", "Luciano Spalletti", "C"));
        Player donnarumma = playerRepository.saveAndFlush(player(3001L, 1, "GK", "G. Donnarumma", italy));

        var found = playerRepository.findByLegacyId(3001L);

        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(donnarumma.getId());
    }

    @Test
    @DisplayName("deleteByTeamId deletes all players for a team and returns affected row count")
    void deleteByTeamId_deletesAndReturnsCount() {
        Team portugal = teamRepository.saveAndFlush(team("Portugal", "Roberto Martinez", "F"));
        Team netherlands = teamRepository.saveAndFlush(team("Netherlands", "Ronald Koeman", "D"));

        Player a = playerRepository.saveAndFlush(player(4001L, 7, "FW", "Cristiano Ronaldo", portugal));
        Player b = playerRepository.saveAndFlush(player(4002L, 10, "MF", "Bruno Fernandes", portugal));
        Player c = playerRepository.saveAndFlush(player(5001L, 4, "DF", "Virgil van Dijk", netherlands));

        int deleted = playerRepository.deleteByTeamId(portugal.getId());

        assertThat(deleted).isEqualTo(2);
        assertThat(playerRepository.findAll())
                .hasSize(1)
                .first()
                .extracting(Player::getId)
                .isEqualTo(c.getId());
    }

    @Nested
    @DisplayName("existsByTeam_Id")
    class ExistsByTeam {

        @Test
        @DisplayName("returns true when any player belongs to the team")
        void returnsTrueWhenPlayersExist() {
            Team england = teamRepository.saveAndFlush(team("England", "Gareth Southgate", "C"));
            playerRepository.saveAndFlush(player(6001L, 9, "FW", "Harry Kane", england));

            boolean exists = playerRepository.existsByTeam_Id(england.getId());

            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("returns false when no players belong to the team")
        void returnsFalseWhenNoPlayers() {
            Team croatia = teamRepository.saveAndFlush(team("Croatia", "Zlatko Dalić", "B"));

            boolean exists = playerRepository.existsByTeam_Id(croatia.getId());

            assertThat(exists).isFalse();
        }
    }

    @Test
    @DisplayName("findByTeam_Name returns all players for a given team name")
    void findByTeam_Name_returnsPlayers() {

        Team germany = teamRepository.saveAndFlush(team("Germany", "Julian Nagelsmann", "A"));
        playerRepository.saveAndFlush(player(7001L, 8, "MF", "Toni Kroos", germany));
        playerRepository.saveAndFlush(player(7002L, 9, "FW", "Kai Havertz", germany));

        List<Player> found = playerRepository.findByTeam_Name("Germany");

        assertThat(found).hasSize(2);
        assertThat(found).extracting(Player::getFullName).containsExactlyInAnyOrder("Toni Kroos", "Kai Havertz");
    }
}