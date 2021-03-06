require File.expand_path('../../../spec_helper', __FILE__)

describe "ARGF.skip" do
  before :each do
    @file1_name = fixture __FILE__, "file1.txt"
    @file2_name = fixture __FILE__, "file2.txt"

    @file2 = File.readlines @file2_name
  end

  it "skips the current file" do
    argf [@file1_name, @file2_name] do
      @argf.read(1)
      @argf.skip
      @argf.gets.should == @file2.first
    end
  end

  it "has no effect when called twice in a row" do
    argf [@file1_name, @file2_name] do
      @argf.read(1)
      @argf.skip
      @argf.skip
      @argf.gets.should == @file2.first
    end
  end

  it "has no effect at end of stream" do
    argf [@file1_name, @file2_name] do
      @argf.read
      @argf.skip
      @argf.gets.should == nil
    end
  end

  # This is similar to the test above, but it uncovered one of the regressions
  # documented in bug #1633. This has been fixed on 1.9 HEAD
  it "has no effect when the current file is the last" do
    argf [@file1_name] do
      lambda { @argf.skip }.should_not raise_error
    end
  end
end
