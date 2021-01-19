# VCF IO
Manage VCF files. This project allow programmers to easily load VCF files into memory:
```java
VariantSet variantSet = VariantSetFactory.createFromFile(new File("my_vcf.vcf"));
```
or create from scratch:
```java
VariantSet variantSet = new VariantSet();
```
from there, you can access to headers:
```java
variantSet.getHeader().getSimpleHeaders().get("fileformat"); // should return "VCFv4.2" or similar
```
or variants
```java
variantSet.getVariants();
variantSet.findVariant("1", 34564);
```

Of course, it is possible to save to disk.
```java
variantSet.save(new File("output.vcf"));
```
