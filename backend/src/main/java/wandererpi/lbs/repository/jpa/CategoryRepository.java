package wandererpi.lbs.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import wandererpi.lbs.entity.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}
