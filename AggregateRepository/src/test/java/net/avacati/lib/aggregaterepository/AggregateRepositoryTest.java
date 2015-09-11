package net.avacati.lib.aggregaterepository;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

public class AggregateRepositoryTest {
    private AggregateRepository<TestEntity, TestDbo> aggregateRepository;
    private DataStore<TestDbo> underlyingInMemoryDataStore;

    @Before
    public void setUp(){
        // Arrange
        underlyingInMemoryDataStore = new InMemoryDataStore<>();
        this.aggregateRepository = AggregateRepository.create(
                underlyingInMemoryDataStore,
                TestEntity::new,
                TestEntity::getDbo);

        this.aggregateRepository.getUnitOfWork();
    }

    @Test
    public void getAllShouldBeEmptyBeforeAdd(){
        Assert.assertTrue(this.aggregateRepository.getInnerRepo().getAll().isEmpty());
    }

    @Test(expected = NoSuchEntityException.class)
    public void getShouldThrowBeforeAdd(){
        this.aggregateRepository.getInnerRepo().get(UUID.randomUUID(), null);
    }

    @Test
    public void getAllShouldBeEmptyAfterAddBeforeSave(){
        // Add a dbo
        TestEntity testEntity = createTestEntity();
        this.aggregateRepository.getInnerRepo().add(testEntity.id, testEntity);

        // Assert
        Assert.assertTrue(this.aggregateRepository.getInnerRepo().getAll().isEmpty());
    }

    @Test
    public void underlyingShouldNotHaveAfterAddBeforeSave(){
        // Add a dbo
        TestEntity testEntity = createTestEntity();
        this.aggregateRepository.getInnerRepo().add(testEntity.id, testEntity);

        // Assert that the underlying store doesn't have it yet.
        Assert.assertNull(this.underlyingInMemoryDataStore.get(testEntity.id));

        // Assert - repo should still return it because of identity map.
        TestEntity resultTestEntity = this.aggregateRepository.getInnerRepo().get(testEntity.id, TestEntity::getDbo);
        Assert.assertEquals(testEntity.id, resultTestEntity.id);
        Assert.assertEquals(testEntity.data, resultTestEntity.data);
    }

    @Test
    public void repoShouldHaveAfterAddBeforeSave(){
        // Add a dbo
        TestEntity testEntity = createTestEntity();
        this.aggregateRepository.getInnerRepo().add(testEntity.id, testEntity);

        // Assert - repo should still return it because of identity map.
        TestEntity resultTestEntity = this.aggregateRepository.getInnerRepo().get(testEntity.id, TestEntity::getDbo);
        Assert.assertEquals(testEntity.id, resultTestEntity.id);
        Assert.assertEquals(testEntity.data, resultTestEntity.data);
    }

    @Test
    public void underlyingShouldHaveAfterSave(){
        // Add a dbo
        TestEntity testEntity = createTestEntity();
        this.aggregateRepository.getInnerRepo().add(testEntity.id, testEntity);

        // Save
        this.aggregateRepository.getUnitOfWork().save();

        // Assert
        final TestDbo testDbo = this.underlyingInMemoryDataStore.get(testEntity.id);
        Assert.assertEquals(testEntity.id, testDbo.id);
        Assert.assertEquals(testEntity.data, testDbo.data);
    }

    @Test
    public void addSaveGetUpdateSaveAndGet(){
        // Arrange an entity saved in the repository
        TestEntity testEntityOriginal = createTestEntity();
        this.aggregateRepository.getInnerRepo().add(testEntityOriginal.id, testEntityOriginal);
        this.aggregateRepository.getUnitOfWork().save();

        // Update some data on it
        TestEntity testEntityAfterAddSaveGet = this.aggregateRepository.getInnerRepo().get(testEntityOriginal.id, TestEntity::getDbo);
        testEntityAfterAddSaveGet.setData("baz");

        // Assert that the new data has not been saved yet.
        TestEntity testEntityBeforeSave = this.aggregateRepository.getInnerRepo().get(testEntityOriginal.id, TestEntity::getDbo);
        Assert.assertEquals(testEntityOriginal.id, testEntityBeforeSave.id);
        Assert.assertEquals(testEntityOriginal.data, testEntityBeforeSave.data);

        // Act
        this.aggregateRepository.getUnitOfWork().save();

        // Assert that the new data has been saved.
        TestEntity testEntityAfterSave = this.aggregateRepository.getInnerRepo().get(testEntityOriginal.id, TestEntity::getDbo);
        Assert.assertEquals(testEntityOriginal.id, testEntityAfterSave.id);
        Assert.assertEquals("baz", testEntityAfterSave.data);
    }

    @Test
    public void multipleGetsShouldReturnSameObject() {
        // Arrange
        TestDbo testDboOriginal = createTestEntity().getDbo();
        this.underlyingInMemoryDataStore.insert(testDboOriginal.id, testDboOriginal);

        // Act
        final TestEntity testEntity1 = this.aggregateRepository.getInnerRepo().get(testDboOriginal.id, TestEntity::getDbo);
        final TestEntity testEntity2 = this.aggregateRepository.getInnerRepo().get(testDboOriginal.id, TestEntity::getDbo);

        // Assert
        Assert.assertTrue(testEntity1 == testEntity2);
    }

    @Test(expected = DuplicateAddException.class)
    public void addTwiceShouldFail() {
        TestEntity testEntityOriginal = createTestEntity();
        this.aggregateRepository.getInnerRepo().add(testEntityOriginal.id, testEntityOriginal);
        this.aggregateRepository.getInnerRepo().add(testEntityOriginal.id, testEntityOriginal);
    }

    @Test
    public void saveTwiceShouldWorkForAdd() {
        // Arrange
        TestEntity testEntityOriginal = createTestEntity();
        this.aggregateRepository.getInnerRepo().add(testEntityOriginal.id, testEntityOriginal);

        // Act - save twice without throwing.
        this.aggregateRepository.getUnitOfWork().save();
        this.aggregateRepository.getUnitOfWork().save();

        // Act - Modify added entity after the insert is finish.
        testEntityOriginal.setData(testEntityOriginal.data + "something to guarantee data is different from before");
        this.aggregateRepository.getUnitOfWork().save();

        // Assert that the latest change was saved _even_ if it was after the initial insert was done.
        final TestDbo resultTestDbo = this.underlyingInMemoryDataStore.get(testEntityOriginal.id);
        Assert.assertEquals(testEntityOriginal.data, resultTestDbo.data);
    }

    @Test
    public void saveTwiceShouldWorkForUpdate() {
        // Arrange into underlying
        TestDbo testDboOriginal = createTestEntity().getDbo();
        this.underlyingInMemoryDataStore.insert(testDboOriginal.id, testDboOriginal);

        // Act - Get the entity.
        TestEntity testEntityOriginal = this.aggregateRepository.getInnerRepo().get(testDboOriginal.id, TestEntity::getDbo);

        // Act - Modify entity.
        testEntityOriginal.setData(testEntityOriginal.data + " modified1");
        this.aggregateRepository.getUnitOfWork().save();

        // Assert that save worked.
        final TestDbo resultTestDbo = this.underlyingInMemoryDataStore.get(testEntityOriginal.id);
        Assert.assertEquals(testDboOriginal.data + " modified1", resultTestDbo.data);

        // Act - Modify entity AGAIN.
        testEntityOriginal.setData(testEntityOriginal.data + " modified2");
        this.aggregateRepository.getUnitOfWork().save();

        // Assert that save worked AGAIN.
        final TestDbo resultTestDbo2 = this.underlyingInMemoryDataStore.get(testEntityOriginal.id);
        Assert.assertEquals(testDboOriginal.data + " modified1 modified2", resultTestDbo2.data);
    }

    private TestEntity createTestEntity() {
        TestEntity testEntity = new TestEntity();
        testEntity.id = UUID.randomUUID();
        testEntity.data = "foobar";
        return testEntity;
    }

    private static class TestDbo {
        public UUID id;
        public String data;
    }

    private static class TestEntity {
        public UUID id;
        public String data;

        public TestEntity(TestDbo testDbo) {
            this.id = testDbo.id;
            this.data = testDbo.data;
        }

        public TestEntity() { }

        public TestDbo getDbo() {
            TestDbo testDbo = new TestDbo();
            testDbo.id = this.id;
            testDbo.data = this.data;
            return testDbo;
        }

        public void setData(String value) {
            this.data = value;
        }

        public String getData() {
            return data;
        }
    }
}
