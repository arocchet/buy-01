# Pull Request Template

## 📝 Description

**Summary of changes:**
<!-- Provide a brief description of the changes in this PR -->

**Fixes/Resolves:**
<!-- Link to the issue this PR addresses, e.g., Fixes #123 -->

## 🛠️ Type of Change

Please mark the relevant option:

- [ ] 🐛 Bug fix (non-breaking change which fixes an issue)
- [ ] ✨ New feature (non-breaking change which adds functionality)
- [ ] 💥 Breaking change (fix or feature that would cause existing functionality to not work as expected)
- [ ] 📚 Documentation update
- [ ] 🔨 Refactoring (no functional changes)
- [ ] 🎨 Style changes (formatting, missing semi colons, etc; no functional changes)
- [ ] ⚡ Performance improvements
- [ ] 🧪 Test additions or updates
- [ ] 🔧 Build/CI changes

## 🎯 Affected Services

Please mark all affected microservices:

- [ ] 🚪 API Gateway
- [ ] 👤 User Service
- [ ] 🛍️ Product Service
- [ ] 📸 Media Service
- [ ] 🎨 Frontend (Angular)
- [ ] 🛠️ CI/CD Pipeline
- [ ] 📊 Documentation

## 🧪 Testing

**Test cases covered:**
- [ ] Unit tests added/updated
- [ ] Integration tests added/updated
- [ ] End-to-end tests added/updated
- [ ] Manual testing completed

**Test instructions:**
<!-- Describe how reviewers can test the changes -->

## 📊 Code Quality

**SonarQube Analysis:**
- [ ] ✅ SonarQube analysis passed
- [ ] ✅ Quality Gate requirements met
- [ ] ✅ No new security vulnerabilities introduced
- [ ] ✅ Code coverage maintained/improved

**Security Checklist:**
- [ ] No sensitive data exposed in logs
- [ ] Input validation implemented where needed
- [ ] Authentication/authorization properly handled
- [ ] SQL injection protection in place
- [ ] XSS protection implemented

## 📸 Screenshots/GIFs

<!-- Include screenshots or GIFs for UI changes -->

## 🔍 Checklist

**Before submitting:**
- [ ] Code follows the project's style guidelines
- [ ] Self-review of code completed
- [ ] Code is commented, particularly in hard-to-understand areas
- [ ] Corresponding changes to documentation made
- [ ] Changes generate no new warnings
- [ ] Tests pass locally
- [ ] Dependent changes merged and published

**Database Changes:**
- [ ] Database migrations included (if applicable)
- [ ] Migration tested on development environment
- [ ] Rollback plan documented

**Deployment Notes:**
- [ ] Environment variables added/changed (document them)
- [ ] Configuration changes required (document them)
- [ ] Service restart required
- [ ] Database migration required

## 🔗 Related Links

<!-- Add links to related issues, documentation, etc. -->

## 👥 Review Guidelines

**For Reviewers:**
1. Check SonarQube analysis results
2. Verify all tests pass
3. Review security implications
4. Check for breaking changes
5. Validate documentation updates

**Priority Level:**
- [ ] 🔴 High Priority (Hotfix/Critical Bug)
- [ ] 🟡 Medium Priority (Feature/Enhancement)
- [ ] 🟢 Low Priority (Documentation/Cleanup)

## 📝 Additional Notes

<!-- Any additional information for reviewers -->