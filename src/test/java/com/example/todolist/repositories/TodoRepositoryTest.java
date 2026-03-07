package com.example.todolist.repositories;

import com.example.todolist.constants.Status;
import com.example.todolist.items.TodoItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class TodoRepositoryTest {

    @Autowired
    private TodoRepository todoRepository;

    private TodoItem notDoneItem;
    private TodoItem doneItem;
    private TodoItem pastDueItem;

    @BeforeEach
    void setUp() {
        todoRepository.deleteAll();

        notDoneItem = new TodoItem();
        notDoneItem.setDescription("Buy groceries");
        notDoneItem.setStatus(Status.NOT_DONE);
        notDoneItem.setCreationDateTime(LocalDateTime.of(2026, 3, 1, 10, 0));
        notDoneItem.setDueDateTime(LocalDateTime.of(2026, 3, 10, 10, 0));
        notDoneItem = todoRepository.save(notDoneItem);

        doneItem = new TodoItem();
        doneItem.setDescription("Clean the house");
        doneItem.setStatus(Status.DONE);
        doneItem.setCreationDateTime(LocalDateTime.of(2026, 3, 1, 9, 0));
        doneItem.setDueDateTime(LocalDateTime.of(2026, 3, 5, 9, 0));
        doneItem.setCompletionDateTime(LocalDateTime.of(2026, 3, 4, 15, 0));
        doneItem = todoRepository.save(doneItem);

        pastDueItem = new TodoItem();
        pastDueItem.setDescription("Submit report");
        pastDueItem.setStatus(Status.PAST_DUE);
        pastDueItem.setCreationDateTime(LocalDateTime.of(2026, 2, 20, 8, 0));
        pastDueItem.setDueDateTime(LocalDateTime.of(2026, 2, 28, 8, 0));
        pastDueItem = todoRepository.save(pastDueItem);
    }

    @Test
    void findAll_shouldReturnAllItems() {
        List<TodoItem> result = todoRepository.findAll();

        assertThat(result).hasSize(3);
        assertThat(result).extracting(TodoItem::getDescription)
                .containsExactlyInAnyOrder("Buy groceries", "Clean the house", "Submit report");
    }

    @Test
    void findAll_whenEmpty_shouldReturnEmptyList() {
        todoRepository.deleteAll();

        List<TodoItem> result = todoRepository.findAll();

        assertThat(result).isEmpty();
    }

    @Test
    void findAllByStatus_withNotDone_shouldReturnOnlyNotDoneItems() {
        List<TodoItem> result = todoRepository.findAllByStatus(Status.NOT_DONE);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getDescription()).isEqualTo("Buy groceries");
        assertThat(result.getFirst().getStatus()).isEqualTo(Status.NOT_DONE);
    }

    @Test
    void findAllByStatus_withDone_shouldReturnOnlyDoneItems() {
        List<TodoItem> result = todoRepository.findAllByStatus(Status.DONE);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getDescription()).isEqualTo("Clean the house");
        assertThat(result.getFirst().getStatus()).isEqualTo(Status.DONE);
    }

    @Test
    void findAllByStatus_withPastDue_shouldReturnOnlyPastDueItems() {
        List<TodoItem> result = todoRepository.findAllByStatus(Status.PAST_DUE);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getDescription()).isEqualTo("Submit report");
        assertThat(result.getFirst().getStatus()).isEqualTo(Status.PAST_DUE);
    }

    @Test
    void findAllByStatus_whenNoItemsMatchStatus_shouldReturnEmptyList() {
        todoRepository.deleteAll();

        List<TodoItem> result = todoRepository.findAllByStatus(Status.DONE);

        assertThat(result).isEmpty();
    }

    @Test
    void findAllByStatus_withMultipleItemsOfSameStatus_shouldReturnAll() {
        TodoItem anotherNotDone = new TodoItem();
        anotherNotDone.setDescription("Walk the dog");
        anotherNotDone.setStatus(Status.NOT_DONE);
        anotherNotDone.setCreationDateTime(LocalDateTime.of(2026, 3, 2, 12, 0));
        anotherNotDone.setDueDateTime(LocalDateTime.of(2026, 3, 12, 12, 0));
        todoRepository.save(anotherNotDone);

        List<TodoItem> result = todoRepository.findAllByStatus(Status.NOT_DONE);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(TodoItem::getDescription)
                .containsExactlyInAnyOrder("Buy groceries", "Walk the dog");
    }

    @Test
    void findById_shouldReturnCorrectItem() {
        Optional<TodoItem> result = todoRepository.findById(notDoneItem.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getDescription()).isEqualTo("Buy groceries");
        assertThat(result.get().getStatus()).isEqualTo(Status.NOT_DONE);
    }

    @Test
    void findById_withNonExistentId_shouldReturnEmpty() {
        Optional<TodoItem> result = todoRepository.findById(999L);

        assertThat(result).isEmpty();
    }

    @Test
    void save_shouldPersistItem() {
        TodoItem newItem = new TodoItem();
        newItem.setDescription("Read a book");
        newItem.setStatus(Status.NOT_DONE);
        newItem.setCreationDateTime(LocalDateTime.of(2026, 3, 5, 14, 0));
        newItem.setDueDateTime(LocalDateTime.of(2026, 3, 15, 14, 0));

        TodoItem saved = todoRepository.save(newItem);

        assertThat(saved.getId()).isNotNull();
        assertThat(todoRepository.findById(saved.getId())).isPresent();
        assertThat(todoRepository.findById(saved.getId()).get().getDescription()).isEqualTo("Read a book");
    }

    @Test
    void markItemsAsPastDue_shouldUpdateOverdueNotDoneItems() {
        TodoItem overdueItem = new TodoItem();
        overdueItem.setDescription("Overdue task");
        overdueItem.setStatus(Status.NOT_DONE);
        overdueItem.setCreationDateTime(LocalDateTime.of(2026, 2, 1, 8, 0));
        overdueItem.setDueDateTime(LocalDateTime.of(2026, 2, 15, 8, 0));
        overdueItem = todoRepository.save(overdueItem);

        LocalDateTime now = LocalDateTime.of(2026, 3, 6, 12, 0);
        todoRepository.markItemsAsPastDue(now);

        TodoItem updated = todoRepository.findById(overdueItem.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(Status.PAST_DUE);
    }

    @Test
    void markItemsAsPastDue_shouldNotUpdateItemsWithFutureDueDate() {
        LocalDateTime now = LocalDateTime.of(2026, 3, 6, 12, 0);
        todoRepository.markItemsAsPastDue(now);

        TodoItem unchanged = todoRepository.findById(notDoneItem.getId()).orElseThrow();
        assertThat(unchanged.getStatus()).isEqualTo(Status.NOT_DONE);
    }

    @Test
    void markItemsAsPastDue_shouldNotUpdateAlreadyPastDueItems() {
        LocalDateTime now = LocalDateTime.of(2026, 3, 6, 12, 0);
        todoRepository.markItemsAsPastDue(now);

        TodoItem unchanged = todoRepository.findById(pastDueItem.getId()).orElseThrow();
        assertThat(unchanged.getStatus()).isEqualTo(Status.PAST_DUE);
    }

    @Test
    void markItemsAsPastDue_shouldNotUpdateDoneItems() {
        LocalDateTime now = LocalDateTime.of(2026, 3, 6, 12, 0);
        todoRepository.markItemsAsPastDue(now);

        TodoItem unchanged = todoRepository.findById(doneItem.getId()).orElseThrow();
        assertThat(unchanged.getStatus()).isEqualTo(Status.DONE);
    }

    @Test
    void markItemsAsPastDue_shouldUpdateMultipleOverdueItems() {
        TodoItem overdueItem1 = new TodoItem();
        overdueItem1.setDescription("Overdue task 1");
        overdueItem1.setStatus(Status.NOT_DONE);
        overdueItem1.setCreationDateTime(LocalDateTime.of(2026, 1, 1, 8, 0));
        overdueItem1.setDueDateTime(LocalDateTime.of(2026, 1, 15, 8, 0));
        overdueItem1 = todoRepository.save(overdueItem1);

        TodoItem overdueItem2 = new TodoItem();
        overdueItem2.setDescription("Overdue task 2");
        overdueItem2.setStatus(Status.NOT_DONE);
        overdueItem2.setCreationDateTime(LocalDateTime.of(2026, 2, 1, 8, 0));
        overdueItem2.setDueDateTime(LocalDateTime.of(2026, 2, 20, 8, 0));
        overdueItem2 = todoRepository.save(overdueItem2);

        LocalDateTime now = LocalDateTime.of(2026, 3, 6, 12, 0);
        todoRepository.markItemsAsPastDue(now);

        assertThat(todoRepository.findById(overdueItem1.getId()).orElseThrow().getStatus())
                .isEqualTo(Status.PAST_DUE);
        assertThat(todoRepository.findById(overdueItem2.getId()).orElseThrow().getStatus())
                .isEqualTo(Status.PAST_DUE);
    }

    @Test
    void markItemsAsPastDue_withNoOverdueItems_shouldNotChangeAnything() {
        LocalDateTime farInThePast = LocalDateTime.of(2020, 1, 1, 0, 0);
        todoRepository.markItemsAsPastDue(farInThePast);

        assertThat(todoRepository.findById(notDoneItem.getId()).orElseThrow().getStatus())
                .isEqualTo(Status.NOT_DONE);
        assertThat(todoRepository.findById(doneItem.getId()).orElseThrow().getStatus())
                .isEqualTo(Status.DONE);
        assertThat(todoRepository.findById(pastDueItem.getId()).orElseThrow().getStatus())
                .isEqualTo(Status.PAST_DUE);
    }
}
